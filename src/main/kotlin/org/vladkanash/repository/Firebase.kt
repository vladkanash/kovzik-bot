package org.vladkanash.repository

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.serialization.responseObject
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.Result.Failure
import com.github.kittinunf.result.Result.Success
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Message(val text: String, val date: LocalDate)

object Firebase {

    init {
        FuelManager.instance.basePath = System.getenv("FIREBASE_URL")
    }

    fun getLastMessage(): Message? {
        val (_, _, result) = "/lastMessage.json"
            .httpGet()
            .responseObject<Message>()

        return getResponse(result)
    }

    fun updateLastMessage(message: Message): Message? {
        val (_, _, result) = "/lastMessage.json"
            .httpPut()
            .body(Json.encodeToString(message))
            .responseObject<Message>()

        return getResponse(result)
    }

    private fun getResponse(result: Result<Message, FuelError>) =
        when (result) {
            is Success -> result.get()
            is Failure -> {
                println(result.getException())
                null
            }
        }
}
