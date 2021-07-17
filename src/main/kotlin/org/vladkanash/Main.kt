package org.vladkanash

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode.MARKDOWN
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.temporal.ChronoUnit.DAYS
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

var lastMessageDate: LocalDate = LocalDate.of(2020, 12, 13)
var lastMessage = "Хотя я и щас ничего"

@ExperimentalTime
fun main() {
    val botToken = System.getenv("TOKEN")
    val kovzikId = System.getenv("KOVZIK_ID").toLong()
    val chatId = System.getenv("GUMBALL_CHAT_ID")

    val bot = bot {
        token = botToken
        dispatch {
            text {
                message.takeIf { it.from?.id == kovzikId }
                    ?.also { lastMessageDate = now() }
                    ?.also { if (it.text != null) lastMessage = it.text!! }
            }
        }
    }

    bot.startPolling()

    runBlocking {
        while (isActive) {
            launch {
                bot.sendMessage(
                    ChatId.fromId(chatId.toLong()),
                    text = getMessage(getDaysPassed(lastMessageDate), lastMessage),
                    parseMode = MARKDOWN
                )
            }
            delay(days(1))
        }
    }
}

private fun getDaysPassed(date: LocalDate) = DAYS.between(date, now())

private fun getMessage(days: Long, lastMessage: String) = """
    Прошло уже *$days* дней с тех пор как Николай общался со своими хорошими друзьями в этой конфе :(
    Его последними словами были: _”$lastMessage”_
""".trimIndent()
