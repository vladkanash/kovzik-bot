package org.vladkanash

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode.MARKDOWN
import dev.inmo.krontab.doInfinity
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlin.time.ExperimentalTime

private const val EVERY_DAY_AT_20 = "0 0 20 * *"

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
                    Message(message.text ?: "", now())
                        .also(Firebase::updateLastMessage)
                }
            }
        }
    }

    bot.startPolling()

    runBlocking {
        doInfinity(EVERY_DAY_AT_20) {
            println("Sending message to chat...")
            sendMessage(bot, chatId)
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

private fun now() = Clock.System.now().toLocalDateTime(currentSystemDefault()).date

private fun composeMessage(): String =
    Firebase.getLastMessage()?.let {
        getMessage(it.date.daysUntilNow(), it.text)
    } ?: "An error occurred 😥"

private fun getMessage(days: Int, lastMessage: String) = """
    ${days.toDayString()} с тех пор как Николай общался со своими хорошими друзьями в этой конфе :(
    Его последними словами были: _”$lastMessage”_
""".trimIndent()

private fun Int.toDayString() = when {
    this in listOf(11, 12, 13, 14) -> "Прошло уже *$this* дней"
    this % 10 == 1 -> "Прошел уже *$this* день"
    this % 10 in listOf(2, 3, 4) -> "Прошло уже *$this* дня"
    else -> "Прошло уже *$this* дней"
}
