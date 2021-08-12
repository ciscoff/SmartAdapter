package s.yarlykov.example.prod.rv

import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import s.yarlykov.decoration.sticky.StickyHolder
import s.yarlykov.example.R
import s.yarlykov.example.prod.domain.MockMessage
import s.yarlykov.lib.smartadapter.adapter.Collector
import s.yarlykov.lib.smartadapter.controller.BindableItemController
import s.yarlykov.lib.smartadapter.holder.BindableViewHolder
import s.yarlykov.lib.smartadapter.model.item.BindableItem

class TimeStampController(@LayoutRes val layoutRes: Int) :
    BindableItemController<MockMessage.Header, TimeStampController.Holder>() {

    override fun createViewHolder(parent: ViewGroup, eventCollector: Collector?): Holder {
        return Holder(parent, layoutRes)
    }

    override fun bind(holder: Holder, item: BindableItem<MockMessage.Header, Holder>) {
        holder.bind(item.data)
    }

    override fun viewType(): Int = layoutRes

    override fun getItemId(data: MockMessage.Header): String {
        return data.uid
    }

    /**
     * ViewHolder
     */
    class Holder(parent: ViewGroup, @LayoutRes layoutRes: Int) :
        BindableViewHolder<MockMessage.Header>(parent, layoutRes), StickyHolder.Header {

        private val dateTimeView = itemView.findViewById<TextView>(R.id.date_time)

        private var groupIdHash: Int = StickyHolder.NO_ID

        override val groupId: Int
            get() = groupIdHash

        override fun bind(data: MockMessage.Header) {
            groupIdHash = data.groupId.hashCode()
            dateTimeView.text = data.title
        }
    }
}