package s.yarlykov.example.prod.domain

import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * TODO Mock Class
 */
sealed class MockMessage {
    /**
     * Уникальный идентификатор элемента модели (для DiffUtil)
     */
    val uid: String = UUID.randomUUID().toString()

    /**
     * Идентификатор группы для функционала Sticky: элементы группируются
     * по какому-то признаку, например по дате.
     */
    lateinit var groupId: String

    /**
     * @param date - дата в формате "2021-08-10T12:10:15+0300"
     */
    data class Header(
        val date: String
    ) : MockMessage() {

        val title: String

        init {
            groupId = date.substringBefore("T")

            title = try {
                LocalDate.parse(groupId, dtfFrom).format(dtfTo)
            } catch (e: Exception) {
                groupId
            }
        }
    }

    data class Data(
        val date: String,
        var isUnread: Boolean
    ) : MockMessage() {

        val dateTime: ZonedDateTime?

        init {
            groupId = date.substringBefore("T")

            dateTime = try {
                ZonedDateTime.parse(date, dtfSrc)
            } catch (e: Exception) {
                null
            }
        }
    }

    companion object {
        private const val DATE_SRC_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ"
        private const val DATE_FROM_PATTERN = "yyyy-MM-dd"
        private const val DATE_TO_PATTERN = "dd MMMM"

        private val dtfSrc = DateTimeFormatter.ofPattern(DATE_SRC_PATTERN)

        // "2021-08-10" -> "10 августа"
        private val dtfFrom = DateTimeFormatter.ofPattern(DATE_FROM_PATTERN)
        private val dtfTo = DateTimeFormatter.ofPattern(DATE_TO_PATTERN)
    }
}