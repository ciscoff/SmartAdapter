package s.yarlykov.lib.smartadapter.holder

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.lib.smartadapter.adapter.Collector

abstract class BindableViewHolder<in T> : BaseViewHolder {

    constructor(
        recyclerView: ViewGroup,
        @LayoutRes layoutId: Int,
        eventCollector: Collector? = null
    ) : super(recyclerView, layoutId, eventCollector)

    constructor(
        itemView: View,
        eventCollector: Collector? = null
    ) : super(itemView, eventCollector)

    abstract fun bind(data: T)
}