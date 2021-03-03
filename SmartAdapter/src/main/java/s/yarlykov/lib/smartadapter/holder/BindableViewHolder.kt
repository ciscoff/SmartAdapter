package s.yarlykov.lib.smartadapter.holder

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

abstract class BindableViewHolder<in T> : BaseViewHolder {

    constructor(
        recyclerView: ViewGroup,
        @LayoutRes layoutId: Int,
        callback: SmartCallback<*>? = null
    ) : super(recyclerView, layoutId, callback)

    constructor(itemView: View, callback: SmartCallback<*>? = null) : super(itemView, callback)

    abstract fun bind(data: T)
}