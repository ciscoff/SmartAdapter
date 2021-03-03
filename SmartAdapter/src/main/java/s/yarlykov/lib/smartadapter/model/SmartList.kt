package s.yarlykov.lib.smartadapter.model

import s.yarlykov.lib.smartadapter.controller.BindableItemController
import s.yarlykov.lib.smartadapter.controller.NoDataItemController
import s.yarlykov.lib.smartadapter.holder.BindableViewHolder
import s.yarlykov.lib.smartadapter.model.item.BaseItem
import s.yarlykov.lib.smartadapter.model.item.BindableItem
import s.yarlykov.lib.smartadapter.model.item.NoDataItem

/**
 * Это фактически использования Java Raw Types
 */
typealias ListItem = BaseItem<*>

class SmartList : ArrayList<ListItem>() {

    companion object {
        fun create() = SmartList()
    }

    /**
     * Добавить элемент, который не содержит данных
     */
    fun addItem(controller: NoDataItemController<*>) {
        insert(size, NoDataItem(controller))
    }

    /**
     * Добавить элемент с данными.
     */
    fun <T : Any, H : BindableViewHolder<T>> addItem(
        data: T,
        controller: BindableItemController<T, H>
    ) {
        insert(size, BindableItem(data, controller))
    }

//    fun <T : Any, C : BindableItemController<T, BindableViewHolder<T>>> addItems(
//        items: List<BindableItem<T, BindableViewHolder<T>>>
//    ) {
//        items.forEach { item ->
//            this.insert(size, item)
//        }
//    }

    fun <T : Any, H : BindableViewHolder<T>> addItems(items: List<BindableItem<T, H>>) {
        items.forEach { item -> this.insert(size, item) }
    }

    private fun insert(index: Int, item: ListItem): SmartList {
        this.add(index, item)
        return this
    }
}