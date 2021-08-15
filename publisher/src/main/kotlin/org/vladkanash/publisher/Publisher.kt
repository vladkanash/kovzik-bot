package org.vladkanash.publisher

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.google.cloud.functions.BackgroundFunction
import com.google.cloud.functions.Context
import com.google.events.cloud.pubsub.v1.Message
import kotlinx.datetime.*
import org.vladkanash.repository.Firebase

class Publisher : BackgroundFunction<Message> {

    override fun accept(payload: Message, context: Context?) {
        val botToken = System.getenv("TOKEN")
        val chatId = System.getenv("GUMBALL_CHAT_ID").toLong()

        bot {
            token = botToken
        }.also { sendMessage(it, chatId) }
        println("Message sent successfully")
    }

    private fun sendMessage(bot: Bot, chatId: Long) {
        composeMessage()?.also {
            bot.sendMessage(
                ChatId.fromId(chatId),
                text = it,
                parseMode = ParseMode.MARKDOWN
            )
        }
    }

    private fun LocalDate.daysUntilNow() = daysUntil(Clock.System.todayAt(TimeZone.currentSystemDefault()))

    private fun composeMessage(): String? {
        val message = Firebase.getLastMessage()
        return when {
            message == null -> "An error occurred 😥"
            message.date.daysUntilNow() < DAYS_THRESHOLD -> null
            else -> getMessageText(message.date.daysUntilNow(), message.text)
        }
    }

    private fun getMessageText(days: Int, lastMessage: String) = """
    ${days.toDayString()} с тех пор как Николай общался со своими хорошими друзьями в этой конфе :(
    Его последними словами были: _”$lastMessage”_
""".trimIndent()

    private fun Int.toDayString() = when {
        this in listOf(11, 12, 13, 14) -> "Прошло уже *$this* дней"
        this % 10 == 1 -> "Прошел уже *$this* день"
        this % 10 in listOf(2, 3, 4) -> "Прошло уже *$this* дня"
        else -> "Прошло уже *$this* дней"
    }

    companion object {
        private const val DAYS_THRESHOLD = 2
    }
}
