package s.yarlykov.lib.smartadapter.controller

import s.yarlykov.lib.smartadapter.holder.BindableViewHolder
import s.yarlykov.lib.smartadapter.model.item.BindableItem

abstract class BindableItemController<T, H : BindableViewHolder<T>> :
    BaseController<H, BindableItem<T, H>>() {

    /**
     * Привязать данные item к holder'у
     */
    fun bind(holder: H, data: T) {
        holder.bind(data)
    }
}