package s.yarlykov.example.prod.rv

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import s.yarlykov.decoration.sticky.StickyHolder
import s.yarlykov.example.R
import s.yarlykov.example.prod.domain.AdapterEvent
import s.yarlykov.example.prod.domain.MockMessage
import s.yarlykov.lib.smartadapter.adapter.Collector
import s.yarlykov.lib.smartadapter.controller.BindableItemController
import s.yarlykov.lib.smartadapter.events.EventWrapper
import s.yarlykov.lib.smartadapter.holder.BindableViewHolder
import s.yarlykov.lib.smartadapter.model.item.BindableItem

class NotificationController(@LayoutRes val layoutRes: Int) :
    BindableItemController<MockMessage.Data, NotificationController.Holder>() {

    override fun createViewHolder(parent: ViewGroup, eventCollector: Collector?): Holder {
        return Holder(parent, layoutRes, eventCollector)
    }

    override fun bind(holder: Holder, item: BindableItem<MockMessage.Data, Holder>) {
        holder.bind(item.data)
    }

    override fun viewType(): Int = layoutRes

    override fun getItemId(data: MockMessage.Data): String {
        return data.uid
    }

    /**
     * ViewHolder
     */
    inner class Holder(parent: ViewGroup, layoutRes: Int, eventCollector: Collector?) :
        BindableViewHolder<MockMessage.Data>(parent, layoutRes, eventCollector), StickyHolder {

        private val statusView: View
        private val doneButton: TextView
        private val deleteButton: TextView
        private val upperLayer: View

        var isEditable: Boolean = false
            private set

        private var groupIdHash: Int = StickyHolder.NO_ID

        override val groupId: Int
            get() = groupIdHash

        init {
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.layout_unc_message_stub, itemView as ViewGroup, true)

            statusView = itemView.findViewById(R.id.notification_status)
            doneButton = itemView.findViewById(R.id.action_button)
            deleteButton = itemView.findViewById(R.id.cancel_button)
            upperLayer = itemView.findViewById(R.id.upper_layer)

            eventCollector?.apply {
                doneButton.setOnClickListener {
                    shadeMessageStatus()
                    it.postDelayed({
                        collectorFlow.tryEmit(
                            EventWrapper<AdapterEvent>(AdapterEvent.Done(adapterPosition))
                        )
                    }, EMISSION_DELAY)
                }

                deleteButton.setOnClickListener {
                    shadeMessageStatus()
                    it.postDelayed({
                        collectorFlow.tryEmit(
                            EventWrapper<AdapterEvent>(AdapterEvent.Delete(adapterPosition))
                        )
                    }, EMISSION_DELAY)
                }
            }
        }

        override fun bind(data: MockMessage.Data) {
            groupIdHash = data.groupId.hashCode()

            doneButton.isEnabled = false
            deleteButton.isEnabled = false
            upperLayer.translationX = 0f

            isEditable = data.isUnread
            drawMessageStatus(data)
        }

        /**
         * Отобразить/скрыть статус сообщения (прочитано/непрочитано).
         */
        private fun drawMessageStatus(
            data: MockMessage.Data,
            @DrawableRes drawableId: Int = R.drawable.circle_red
        ) {
            val status = when (data.isUnread) {
                true -> {
                    getDrawable(drawableId)
                }
                false -> {
                    null
                }
            }

            status?.let {
                statusView.background = it
                statusView.visibility = View.VISIBLE
            } ?: run {
                statusView.visibility = View.INVISIBLE
            }
        }

        private fun shadeMessageStatus(@DrawableRes drawableId: Int = R.drawable.circle_gray) {
            statusView.background = getDrawable(drawableId)
        }

        private fun getDrawable(@DrawableRes drawableId: Int): Drawable? {
            return ContextCompat.getDrawable(itemView.context, drawableId)
        }
    }

    companion object {
        // События отправляются с задержкой, чтобы отработала анимация
        private const val EMISSION_DELAY = 200L
    }
}