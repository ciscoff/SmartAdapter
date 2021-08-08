package s.yarlykov.example.prod.domain

/**
 * Данные для отрисовки элементов UI о количестве непрочитанных сообщений, предлагаемом действии.
 *
 * @property title - например "Нет новых"
 * @property proposal - например "Удалить все"
 * @property action - действие, например [UserAction.DeleteAll]
 */
data class ControlBarState(
    val title: String,
    val proposal: String,
    val action: UserAction
)