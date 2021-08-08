package s.yarlykov.example.prod.rv

import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.LayoutRes
import s.yarlykov.example.R
import s.yarlykov.example.prod.domain.AdapterEvent
import s.yarlykov.lib.smartadapter.adapter.Collector
import s.yarlykov.lib.smartadapter.controller.NoDataItemController
import s.yarlykov.lib.smartadapter.events.EventWrapper
import s.yarlykov.lib.smartadapter.holder.BaseViewHolder
import s.yarlykov.lib.smartadapter.model.item.NoDataItem

class FailureController(@LayoutRes val layoutRes: Int) :
    NoDataItemController<FailureController.Holder>() {

    override fun createViewHolder(
        parent: ViewGroup,
        eventCollector: Collector?
    ): Holder {
        return Holder(parent, layoutRes, eventCollector)
    }

    override fun bind(holder: Holder, item: NoDataItem<Holder>) {
        // nothing to do
    }

    override fun viewType(): Int {
        return layoutRes
    }

    override fun getItemHash(item: NoDataItem<Holder>): String {
        return item.hashCode().toString()
    }

    /**
     * ViewHolder
     */
    class Holder(
        parent: ViewGroup,
        @LayoutRes val layoutId: Int,
        eventCollector: Collector?
    ) :
        BaseViewHolder(parent, layoutId, eventCollector) {

        // Клик по кнопке
        init {
            eventCollector?.apply {
                itemView.findViewById<Button>(R.id.button_update).setOnClickListener {
                    collectorFlow.tryEmit(EventWrapper<AdapterEvent>(AdapterEvent.Update))
                }
            }
        }
    }
}