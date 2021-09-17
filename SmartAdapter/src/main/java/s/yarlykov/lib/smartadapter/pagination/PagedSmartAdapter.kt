package s.yarlykov.lib.smartadapter.pagination

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.lib.smartadapter.adapter.SmartAdapter
import s.yarlykov.lib.smartadapter.events.EventWrapper

/**
 * Кусок кода паджинации из проекта IziMaster::ChatActivity.kt
 *
private val swipeRefreshListener: () -> Unit = {
if (!isUpdating) {
isUpdating = true

// Если текущее количество показанных сообщений кратно CHAT_PAGE_SIZE,
// то существует вероятность, что ещё не все сообщения чата скачаны.
if (model.isNotEmpty() && (topicId != 0) && (model.size % CHAT_PAGE_SIZE == 0)) {
viewModel.requestMorePages(topicId, model.size / CHAT_PAGE_SIZE + 1)
} else {
isUpdating = false
}
}
}
 *
 */


/**
 * Нужно добавить RecyclerView.LayoutManager, который следит за приближением к хвосту списка
 * при прокрутке. Если осталось непросмотрено N элементов, то выдается сигнал TakeMore. Сообщение
 * TakeMore уходит во внешний мир через коллекторы в обертке EventWrapper<TakeMore>. Внешний
 * код реагирует и подкачивает следующую порцию данных.
 *
 * Вариант реализации TakeMore, где lastIndex - длина текущей модели.
 *
 * data class TakeMore(val lastIndex: Int)
 */
class PagedSmartAdapter(private val prefetchDistance: Int) : SmartAdapter() {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        initPaginationListener(recyclerView)
    }

    /**
     * TODO Пока работаем только с LinearLayoutManager
     */
    private fun initPaginationListener(recyclerView: RecyclerView) {

        val lm = recyclerView.layoutManager

        lm.hashCode()

        val layoutManager = (recyclerView.layoutManager as? LinearLayoutManager)
            ?: throw Exception("Only LinearLayoutManager is supported")

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val totalItemsCount = layoutManager.itemCount
                val lastIndex = if (itemCount > 0) itemCount - 1 else 0
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                val numVisibleItems = lastVisiblePosition - firstVisiblePosition

                if (totalItemsCount - lastVisiblePosition < prefetchDistance) {
                    collector.collectorFlow.tryEmit(EventWrapper(TakeMore(lastIndex)))
                }
            }
        })
    }

}