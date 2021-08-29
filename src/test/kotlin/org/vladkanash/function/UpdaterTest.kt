package org.vladkanash.function

import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.vladkanash.repository.Firebase
import org.vladkanash.repository.Message
import java.io.BufferedReader
import java.io.Reader

@ExperimentalSerializationApi
@ExtendWith(MockKExtension::class)
class UpdaterTest {

    @InjectMockKs
    lateinit var updater: Updater

    @MockK
    lateinit var firebase: Firebase

    @MockK
    lateinit var request: HttpRequest

    @MockK
    lateinit var response: HttpResponse

    @MockK
    lateinit var reader: BufferedReader

    @BeforeEach
    fun setUp() {
        every { firebase.updateLastMessage(any()) } returnsArgument 0
        every { request.queryParameters } returns mapOf("token" to listOf("test-verify-token"))
        every { request.reader } returns reader
        every { reader.close() } just runs
        mockkStatic(Reader::readText)
    }

    @Test
    fun `Should throw exception if verification token is missing`() {
        every { request.queryParameters } returns emptyMap()

        assertThrows<IllegalArgumentException> {
            updater.service(request, response)
        }
    }

    @Test
    fun `Should not update message if author is not matching`() {
        every { reader.readText() } returns generateUpdate(from = 1122334455)

        updater.service(request, response)

        verify(exactly = 0) { firebase.updateLastMessage(any()) }
    }

    @Test
    fun `Should update message if author is matching`() {
        every { reader.readText() } returns generateUpdate(from = 123456789)

        updater.service(request, response)

        verify(exactly = 1) {
            firebase.updateLastMessage(
                Message(text = "test", date = now().toLocalDateTime(currentSystemDefault()).date)
            )
        }
    }

    companion object {

        //language=json
        fun generateUpdate(from: Long) = """{
          "update_id": 65432523523,
          "message": {
            "message_id": 8744,
            "from": {
              "id": $from,
              "is_bot": false,
              "first_name": "Vlad",
              "username": "test-user",
              "language_code": "en"
            },
            "chat": {
              "id": -6475463543,
              "title": "Chat title",
              "type": "group",
              "all_members_are_administrators": true
            },
            "date": 1630231332,
            "text": "test"
          }
        }
        """
    }
}
