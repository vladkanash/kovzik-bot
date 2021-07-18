package org.vladkanash

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode.MARKDOWN
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import kotlinx.datetime.Instant.Companion.fromEpochMilliseconds
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    val botToken = System.getenv("TOKEN")
    val kovzikId = System.getenv("KOVZIK_ID").toLong()
    val chatId = System.getenv("GUMBALL_CHAT_ID").toLong()

    val bot = bot {
        token = botToken
        dispatch {
            text {
                if (message.from?.id == kovzikId) {
                    Message(message.text ?: "", message.date.toLocalDate())
                        .also(Firebase::updateLastMessage)
                }
            }
        }
    }

    bot.startPolling()

    runBlocking {
        while (isActive) {
            launch { sendMessage(bot, chatId) }
            delay(days(1))
        }
    }
}

private fun sendMessage(bot: Bot, chatId: Long) {
    bot.sendMessage(
        ChatId.fromId(chatId),
        text = composeMessage(),
        parseMode = MARKDOWN
    )
}

private fun LocalDate.daysUntilNow() = daysUntil(Clock.System.todayAt(currentSystemDefault()))

private fun Long.toLocalDate() = fromEpochMilliseconds(this)
    .toLocalDateTime(currentSystemDefault()).date

private fun composeMessage(): String =
    Firebase.getLastMessage()?.let {
        getMessage(it.date.daysUntilNow(), it.text)
    } ?: "An error occurred 😥"

private fun getMessage(days: Int, lastMessage: String) = """
    Прошло уже *$days* дней с тех пор как Николай общался со своими хорошими друзьями в этой конфе :(
    Его последними словами были: _”$lastMessage”_
""".trimIndent()
