package s.yarlykov.lib.smartadapter.adapter

import io.reactivex.Observer
import kotlinx.coroutines.flow.MutableSharedFlow
import s.yarlykov.lib.smartadapter.holder.EventWrapper
import s.yarlykov.lib.smartadapter.holder.SmartCallback

/**
 * Это Event Collector - способы отправки сообщений из ViewHolder'ов потребителям.
 */
interface Collector {
    val collectorRx: Observer<EventWrapper<Any>>
    val collectorFlow: MutableSharedFlow<EventWrapper<Any>>
    val smartCallback: SmartCallback<Any?>
}