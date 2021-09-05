package s.yarlykov.example.prod.data

import s.yarlykov.example.prod.domain.MockMessage
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object ModelGenerator {
    private const val MESSAGES_PER_DAY = 4
    private const val dateFormat = "dd.MM.yyyy"
    private const val dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ssZ"

    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat)
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)

    fun createModel(quantity: Int = 10, date: LocalDate): List<MockMessage> {

        val messages = mutableListOf<MockMessage>()

        val n = quantity / MESSAGES_PER_DAY
        val m = quantity % MESSAGES_PER_DAY

        repeat(n) { i ->
            messages.addAll(messages.size, generate(MESSAGES_PER_DAY, date.daysAgo(i.toLong())))
        }
        messages.addAll(messages.size, generate(m, date.daysAgo(n.toLong())))

        return messages
    }

    private fun generate(times: Int, date: ZonedDateTime): List<MockMessage> {
        val list = mutableListOf<MockMessage>()

        repeat(times) {
            val message = MockMessage.Data(
                date = date.toSz(),
                isUnread = Random.nextBoolean()
            )
            list.add(message)
        }

        return list
    }

    private fun LocalDate.daysAgo(shiftDays: Long): ZonedDateTime {
        return minusDays(shiftDays)
            .atStartOfDay(ZoneId.systemDefault())
            .plusHours(Random.nextLong(8, 20))
            .plusMinutes(Random.nextLong(0, 60))
    }

    private fun ZonedDateTime.toSz(dateOnly: Boolean = false): String {
        return format(if (dateOnly) dateFormatter else dateTimeFormatter)
    }
}