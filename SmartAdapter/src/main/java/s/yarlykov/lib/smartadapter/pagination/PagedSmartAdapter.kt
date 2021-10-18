package s.yarlykov.lib.smartadapter.pagination

import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import s.yarlykov.lib.smartadapter.adapter.SmartAdapter
import s.yarlykov.lib.smartadapter.events.EventWrapper
import s.yarlykov.lib.smartadapter.extensions.appMainScope
import s.yarlykov.lib.smartadapter.extensions.throttleFirst


/**
 * TODO 1:
 * Возможность начальной загрузки с произвольного индекса, а не только с 0 (может пригодиться,
 * если приложение восстановлено и нужно вернуть UI в прежнее состояние). Также придется хранить
 * в onSaveInstanceState последнее значение "верхнего" индекса. ViewModel как хранилка не поможет.
 *
 * TODO 2:
 * Добавить поддержку GridLayoutManager в bindPaginationListener. Там придется высчитывать с
 * учетом количества элементов в строке.
 *
 * @property prefetchDistance - размер страницы
 */
@ExperimentalCoroutinesApi
class PagedSmartAdapter(private val prefetchDistance: Int) :
    SmartAdapter(), CoroutineScope by appMainScope() {

    private var job: Job? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.doOnNextLayout {
            job = bindPaginationListener(recyclerView)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        job?.cancel()
    }

    private fun bindPaginationListener(recyclerView: RecyclerView): Job {
        val layoutManager = (recyclerView.layoutManager as? LinearLayoutManager)
            ?: throw Exception(EX_LM_RESTRICTIONS)

        return launch {
            pagingFlow(recyclerView, layoutManager).throttleFirst(THROTTLE_TIMEOUT)
                .collect { event -> collector.collectorFlow.tryEmit(event) }
        }
    }

    private fun pagingFlow(recyclerView: RecyclerView, layoutManager: LinearLayoutManager) =
        callbackFlow {

            val scrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    // Прокрутку к верхним элементам игнорировать
                    if (dy <= 0) {
                        return
                    }

                    val lastModelPosition = if (itemCount > 0) itemCount - 1 else 0
                    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

                    if (lastModelPosition - lastVisiblePosition < prefetchDistance) {
                        trySend(EventWrapper(TakeMore(lastVisiblePosition)))
                    }
                }
            }

            recyclerView.addOnScrollListener(scrollListener)
            awaitClose { recyclerView.removeOnScrollListener(scrollListener) }
        }

    companion object {
        private const val EX_LM_RESTRICTIONS = "Only LinearLayoutManager is supported"
        private const val THROTTLE_TIMEOUT = 1500L
    }
}