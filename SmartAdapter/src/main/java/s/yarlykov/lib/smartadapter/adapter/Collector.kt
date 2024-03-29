package s.yarlykov.lib.smartadapter.adapter

import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.MutableSharedFlow
import s.yarlykov.lib.smartadapter.events.EventWrapper
import s.yarlykov.lib.smartadapter.events.SmartCallback

/**
 * Это Event Collector - способы отправки сообщений из ViewHolder'ов потребителям.
 */
interface Collector {
    val collectorRx: PublishSubject<EventWrapper<Any>>
    val collectorFlow: MutableSharedFlow<EventWrapper<Any>>
    val smartCallback: SmartCallback<Any?>
}