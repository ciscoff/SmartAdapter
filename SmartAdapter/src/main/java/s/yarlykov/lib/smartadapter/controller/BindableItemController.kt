package s.yarlykov.lib.smartadapter.controller

import s.yarlykov.lib.smartadapter.holder.BindableViewHolder
import s.yarlykov.lib.smartadapter.model.item.BindableItem

abstract class BindableItemController<T, H : BindableViewHolder<T>> :
    BaseController<H, BindableItem<T, H>>() {

    /**
     * Привязать данные item к holder 'у
     */
    fun bind(holder: H, data: T) {
        holder.bind(data)
    }

    override fun getItemId(item: BindableItem<T, H>): String {
        /*return System.identityHashCode(item.data).toString()*/
        return getItemId(item.data)
    }

    override fun getItemHash(item: BindableItem<T, H>): String {
        return getItemHash(item.data)
    }

    abstract fun getItemId(data: T): String

    private fun getItemHash(data: T): String {
        return (data?.hashCode() ?: 0).toString()
    }
}