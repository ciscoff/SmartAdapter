package s.yarlykov.lib.smartadapter.pagination

import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.lib.smartadapter.adapter.SmartAdapter
import s.yarlykov.lib.smartadapter.events.EventWrapper

//  todo: Кусок кода пагинации из проекта IziMaster::ChatActivity.kt
//
//  private val swipeRefreshListener: () -> Unit = {
//      if (!isUpdating) {
//          isUpdating = true
//
//          todo Если текущее количество показанных сообщений кратно CHAT_PAGE_SIZE,
//          todo то существует вероятность, что ещё не все сообщения чата скачаны.
//          if (model.isNotEmpty() && (topicId != 0) && (model.size % CHAT_PAGE_SIZE == 0)) {
//              viewModel.requestMorePages(topicId, model.size / CHAT_PAGE_SIZE + 1)
//          } else {
//              isUpdating = false
//          }
//      }
//  }

/**
 * Используется RecyclerView.OnScrollListener, который следит за приближением к хвосту списка
 * при прокрутке. Если осталось непросмотрено N элементов, то выдается сигнал TakeMore. Сообщение
 * TakeMore уходит во внешний мир в обертке EventWrapper<TakeMore> через коллекторы. Внешний
 * код реагирует и подкачивает следующую порцию данных.
 *
 * Вариант реализации TakeMore, где lastIndex - длина текущей модели:
 *      data class TakeMore(val lastIndex: Int)
 */

/**
 * TODO 1:
 * Возможность начальной загрузки с произвольного индекса, а не только в 0. Это может пригодиться,
 * если приложение было убито и восстановлено системой. Тогда придется сделать пагинацию "вверх",
 * т.е. в сторону индекса 0. Также придется хранить в onSaveInstanceState последнее значение
 * "верхнего" индекса. ViewModel как хранилка не поможет.
 *
 * @property prefetchDistance - размер страницы
 */
class PagedSmartAdapter(private val prefetchDistance: Int) : SmartAdapter() {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        /**
         * NOTE: Если адаптер назначен раньше, чем LayoutManager, то мы получим Exception
         * в initPaginationListener, потому что там val layoutManager окажется равной null.
         * Поэтому подписываемся на следующий layout RecyclerView и уже после выполняем
         * инициализацию пагинации.
         *
         * Эта подписка автоматически удаляется после первого срабатывания.
         */
        recyclerView.doOnNextLayout {
            bindPaginationListener(recyclerView)
        }
    }

    /**
     * TODO Пока работаем только с LinearLayoutManager.
     *
     * Также надо добавить поддержку GridLayoutManager. Там придется высчитывать с учетом
     * количества элементов в строке.
     */
    private fun bindPaginationListener(recyclerView: RecyclerView) {
        val layoutManager = (recyclerView.layoutManager as? LinearLayoutManager)
            ?: throw Exception("Only LinearLayoutManager is supported")

        /**
         * Если OnScrollListener сработал, значит что-то прокрутили, значит в адаптере
         * есть какая-то модель. Поэтому itemCount должен быть больше 0. Но на всякий случай...
         */
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastModePosition = if (itemCount > 0) itemCount - 1 else 0
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

                if (lastModePosition - lastVisiblePosition < prefetchDistance) {
                    val event = EventWrapper(TakeMore(lastVisiblePosition))
                    collector.collectorFlow.tryEmit(event)
                    collector.collectorRx.onNext(event)
                }
            }
        })
    }
}