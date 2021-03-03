package s.yarlykov.lib.smartadapter.model.item

import s.yarlykov.lib.smartadapter.controller.BaseController
import s.yarlykov.lib.smartadapter.controller.NoDataItemController
import s.yarlykov.lib.smartadapter.holder.BaseViewHolder

@Suppress("UNCHECKED_CAST")
class NoDataItem<H : BaseViewHolder>(controller: NoDataItemController<H>) :
    BaseItem<H>(controller as BaseController<H, BaseItem<H>>)