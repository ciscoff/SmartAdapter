package s.yarlykov.example.prod.domain

import java.util.*

/**
 * TODO Mock Class
 */
sealed class MockUncMessage {
    val uid: String = UUID.randomUUID().toString()

    data class Header(
        val date: String
    ) : MockUncMessage()

    data class Data(
        var isUnread: Boolean,
        val date: String
    ) : MockUncMessage()
}