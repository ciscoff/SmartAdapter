package s.yarlykov.lib.smartadapter.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.lib.smartadapter.adapter.Collector

abstract class BaseViewHolder : RecyclerView.ViewHolder {

    /**
     * Каналы отправки сообщений подписчикам (альтернатива callback'у)
     */
    protected var eventCollector: Collector? = null

    constructor(
        parent: ViewGroup,
        @LayoutRes layoutId: Int,
        eventCollector: Collector? = null
    ) : super(
        LayoutInflater.from(parent.context)
            .inflate(
                layoutId,
                parent,
                false
            )
    ) {
        this.eventCollector = eventCollector
    }

    constructor(
        parent: ViewGroup,
        view: View,
        eventCollector: Collector? = null
    ) : super(view) {
        this.eventCollector = eventCollector
    }

    constructor(
        itemView: View,
        eventCollector: Collector? = null
    ) : super(itemView) {
        this.eventCollector = eventCollector
    }

    /**
     * Когда view инвалидируется, то адаптер вызовет этот метод для очистки ресурсов
     * из своего onViewRecycled
     */
    open fun clear() {
        eventCollector = null
    }
}