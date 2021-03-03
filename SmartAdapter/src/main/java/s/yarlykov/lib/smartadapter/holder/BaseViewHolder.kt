package s.yarlykov.lib.smartadapter.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class BaseViewHolder : RecyclerView.ViewHolder {

    /**
     * star-projection 'in: Nothing, out : Any?'
     *
     * Наследники могут принимать и отдавать что угодно.
     */
    var callback: SmartCallback<*>? = null

    /**
     * Канал отправки сообщений подписчикам (альтернатива callback'у)
     */
    private val events = PublishSubject.create<EventWrapper<Any>>()
    val eventsObservable: Observable<EventWrapper<Any>> by lazy {
        events.hide()
    }

    constructor(
        parent: ViewGroup,
        @LayoutRes layoutId: Int,
        callback: SmartCallback<*>? = null
    ) : super(
        LayoutInflater.from(parent.context)
            .inflate(
                layoutId,
                parent,
                false
            )
    ) {
        this.callback = callback
    }

    constructor(itemView: View, callback: SmartCallback<*>? = null) : super(itemView) {
        this.callback = callback
    }

    /**
     * Когда view инвалидируется, то адаптер вызовет этот метод для очистки ресурсов
     * из своего onViewRecycled
     */
    open fun clear() {}
}