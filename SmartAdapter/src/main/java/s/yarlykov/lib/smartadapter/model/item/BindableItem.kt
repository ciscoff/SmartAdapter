package s.yarlykov.lib.smartadapter.model.item

import s.yarlykov.lib.smartadapter.controller.BaseController
import s.yarlykov.lib.smartadapter.controller.BindableItemController
import s.yarlykov.lib.smartadapter.holder.BindableViewHolder

/**
 * Базовый контейнер для элемента модели, которая ИМЕЕТ данные <T>
 */
@Suppress("UNCHECKED_CAST")
open class BindableItem<T, H : BindableViewHolder<T>>(
    val data: T,
    controller: BindableItemController<T, H>
) : BaseItem<H>(controller as BaseController<H, BaseItem<H>>)