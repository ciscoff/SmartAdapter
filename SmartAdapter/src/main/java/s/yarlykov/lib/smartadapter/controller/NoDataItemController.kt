package s.yarlykov.lib.smartadapter.controller

import s.yarlykov.lib.smartadapter.holder.BaseViewHolder
import s.yarlykov.lib.smartadapter.model.item.NoDataItem

abstract class NoDataItemController<H : BaseViewHolder> : BaseController<H, NoDataItem<H>>()