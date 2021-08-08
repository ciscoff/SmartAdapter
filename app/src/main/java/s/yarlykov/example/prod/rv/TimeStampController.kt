package s.yarlykov.example.prod.rv

import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import s.yarlykov.decoration.sticky.StickyHolder
import s.yarlykov.example.R
import s.yarlykov.example.prod.domain.MockUncMessage
import s.yarlykov.lib.smartadapter.adapter.Collector
import s.yarlykov.lib.smartadapter.controller.BindableItemController
import s.yarlykov.lib.smartadapter.holder.BindableViewHolder
import s.yarlykov.lib.smartadapter.model.item.BindableItem

class TimeStampController(@LayoutRes val layoutRes: Int) :
    BindableItemController<MockUncMessage.Header, TimeStampController.Holder>() {

    override fun createViewHolder(parent: ViewGroup, eventCollector: Collector?): Holder {
        return Holder(parent, layoutRes)
    }

    override fun bind(holder: Holder, item: BindableItem<MockUncMessage.Header, Holder>) {
        holder.bind(item.data)
    }

    override fun viewType(): Int = layoutRes

    override fun getItemId(data: MockUncMessage.Header): String {
        return data.uid
    }

    /**
     * ViewHolder
     */
    class Holder(parent: ViewGroup, @LayoutRes layoutRes: Int) :
        BindableViewHolder<MockUncMessage.Header>(parent, layoutRes), StickyHolder {

        private val dateTimeView = itemView.findViewById<TextView>(R.id.date_time)

        override val id: Int
            get() = adapterPosition

        override fun bind(data: MockUncMessage.Header) {
            dateTimeView.text = data.date
        }
    }
}