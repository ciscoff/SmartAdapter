package s.yarlykov.lib.smartadapter.adapter

import android.util.SparseArray
import android.view.ViewGroup
import androidx.core.util.set
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import s.yarlykov.lib.smartadapter.controller.BaseController
import s.yarlykov.lib.smartadapter.holder.BaseViewHolder
import s.yarlykov.lib.smartadapter.holder.EventWrapper
import s.yarlykov.lib.smartadapter.holder.SmartCallback
import s.yarlykov.lib.smartadapter.model.SmartList
import s.yarlykov.lib.smartadapter.model.item.BaseItem

/**
 * Алгоритм работы следующий:
 * Адаптер использует в качестве модели SmartList Item'ов. Каждый Item имеет ссылку на свой
 * контроллер и опционально ссылку на 'data: T' (если данные имеются). На каждый viewType создается
 * отдельный контроллер. Зависимость такова: ControllerA -> layoutIdA -> ViewHolderA, то есть
 * layoutIdA - это viewType, а ViewHolderA знает только про иерархию внутри layoutIdA. Универсальные
 * холдеры под несколько layoutId не применяются.
 *
 * Для создания ViewHolder'а и binding'а адаптер использует контроллер Item'а.
 * - Создание холдера: контроллер инфлейтит view с помощью layoutId, создает и возвращает холдер.
 * - Binding: адаптер передает контроллеру созданный ранее холдер и ссылку на Item.
 */

class SmartAdapter(private val callback: SmartCallback<Any?>? = null) :
    RecyclerView.Adapter<BaseViewHolder>() {

    /**
     * Модель данных
     */
    private val model = SmartList()

    /**
     * Список контроллеров для текущей модели данных
     */
    private val supportedControllers = SparseArray<BaseController</*H*/*, /*I*/*>>()

    /**
     * Рестрансляторы событий от ViewHolder'ов. События можно отправлять тремя способами:
     * - rx relay
     * - shared flow
     * - callback
     */
    private val collector = object : Collector, SmartCallback<Any?> {

        override val collectorRx: PublishSubject<EventWrapper<Any>> by lazy {
            PublishSubject.create()
        }

        override val collectorFlow: MutableSharedFlow<EventWrapper<Any>> by lazy {
            MutableSharedFlow()
        }

        override val smartCallback: SmartCallback<Any?> = this

        override fun call(arg: Any?) {
            callback?.call(arg)
        }
    }

    /**
     * Поставщики данных внешним потребителям
     */
    val eventBus = collector.collectorRx.hide()

    val eventFlow = collector.collectorFlow.asSharedFlow()

    /**
     * Создаем ViewHolder и привязываем его к шине данных.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return supportedControllers[viewType].createViewHolder(parent, collector)
    }

    /**
     * Короче вот этот ад с явным кастом и type projection позволил запуститься.
     */
    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        // Вот этот код не запуститься, потому что автоматическая type projection
        // блокирует.
//        model[position].let { item ->
//            item.controller.bind(holder, item)
//        }

        val baseItem = model[position] as BaseItem<BaseViewHolder>
        val controller =
            baseItem.controller as BaseController<in BaseViewHolder, in BaseItem<BaseViewHolder>>

        controller.bind(holder, baseItem)
    }

    override fun getItemViewType(position: Int): Int {
        return model[position].controller.viewType()
    }

    override fun getItemCount(): Int = model.size

    /**
     * View холдера @holder перешло в состояние Recycled
     */
    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        holder.clear()
    }

    /**
     * Обновляем модель данных
     */
    fun updateModel(smartList: SmartList) {

        model.apply {
            clear()
            addAll(smartList)
        }

        // Обновить контроллеры
        updateControllers()
        notifyDataSetChanged()
    }

    /**
     * Обновляем модель контроллеров.
     * Модель данных состоит из Item'ов, каждый из которых содержит ссылку на свой контроллер.
     */
    private fun updateControllers() {
        supportedControllers.apply {
            clear()
            model.forEach { item -> this[item.controller.viewType()] = item.controller }
        }
    }
}