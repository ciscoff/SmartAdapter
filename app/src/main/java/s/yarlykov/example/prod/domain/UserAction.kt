package s.yarlykov.example.prod.domain

/**
 * Действия с сообщениями
 */
sealed class UserAction {
    object MarkAllAsRead : UserAction()
    object DeleteAll : UserAction()
    data class MarkAsRead(val position: Int) : UserAction()
    data class Delete(val position: Int) : UserAction()
}