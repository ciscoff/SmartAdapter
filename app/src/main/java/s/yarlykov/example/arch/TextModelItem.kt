package s.yarlykov.example.arch

import s.yarlykov.lib.smartadapter.controller.BindableItemController
import s.yarlykov.lib.smartadapter.holder.BindableViewHolder
import s.yarlykov.lib.smartadapter.model.item.BindableItem

class TextModelItem(
    data: TextModel,
    controller1: Controller1
) : BindableItem<TextModel, BindableViewHolder<TextModel>>(
    data,
    controller1 as BindableItemController<TextModel, BindableViewHolder<TextModel>>
)