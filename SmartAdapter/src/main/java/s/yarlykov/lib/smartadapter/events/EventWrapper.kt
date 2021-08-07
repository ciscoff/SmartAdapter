package s.yarlykov.lib.smartadapter.events

/**
 * Используется для обертки сообщений от ViewHolder'ов через EventBus.
 * Подходит и для работы с LiveData.
 *
 * Доп инфа: https://bit.ly/2Z5wgqD
 */
class EventWrapper<out T>(private val content: T) {

    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}