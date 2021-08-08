package s.yarlykov.example.prod.rv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.example.R
import s.yarlykov.lib.smartadapter.adapter.Collector
import s.yarlykov.lib.smartadapter.controller.NoDataItemController
import s.yarlykov.lib.smartadapter.holder.BaseViewHolder
import s.yarlykov.lib.smartadapter.model.item.NoDataItem

class StubController(@LayoutRes val layoutRes: Int) :
    NoDataItemController<StubController.Holder>() {

    override fun createViewHolder(parent: ViewGroup, eventCollector: Collector?): Holder {
        return Holder(parent, layoutRes)
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
     * @param layoutId - id макета корневого элемента. Если им является оболочка
     * [s.yarlykov.lib.smartadapter.widgets.ShimmerLayout], то в методе init {} добавляем в неё
     * дочерние Views.
     */
    class Holder(parent: ViewGroup, @LayoutRes val layoutId: Int) :
        BaseViewHolder(parent, layoutId) {

        init {
            if (layoutId == R.layout.layout_shimmer_item) {
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.layout_stub_card_view, itemView as ViewGroup, true)
            }
        }
    }
}