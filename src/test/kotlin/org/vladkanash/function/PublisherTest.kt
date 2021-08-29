package org.vladkanash.function

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.vladkanash.repository.Firebase
import org.vladkanash.repository.Message
import com.google.events.cloud.pubsub.v1.Message as PubSubMessage

@ExtendWith(MockKExtension::class)
internal class PublisherTest {

    lateinit var publisher: Publisher

    @MockK
    lateinit var firebase: Firebase

    @MockK(relaxed = true)
    lateinit var botMockk: Bot

    @BeforeEach
    fun setUp() {
        mockkStatic(::bot)
        every { bot(any()) } returns botMockk
        publisher = Publisher(firebase)
    }

    @Test
    fun `Should not publish message if days passed less than threshold`() {
        every { firebase.getLastMessage() } returns Message(
            text = "test message",
            date = now().toLocalDateTime(currentSystemDefault()).date
        )

        publisher.accept(PubSubMessage(), null)

        verify(exactly = 0) { botMockk.sendMessage(any(), any(), any()) }
    }

    @Test
    fun `Should publish message if days passed exceeds threshold`() {
        every { firebase.getLastMessage() } returns Message(
            text = "test message",
            date = now()
                .minus(5, DAY, currentSystemDefault())
                .toLocalDateTime(currentSystemDefault()).date
        )

        publisher.accept(PubSubMessage(), null)

        verify(exactly = 1) { botMockk.sendMessage(any(), any(), any()) }
    }
}
