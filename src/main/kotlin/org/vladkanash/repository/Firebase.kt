package org.vladkanash.repository

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.fuel.serialization.responseObject
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.Result.Failure
import com.github.kittinunf.result.Result.Success
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val ACCESS_TOKEN_PARAM = "access_token"

@Serializable
data class Message(val text: String, val date: LocalDate)

class Firebase {

    private val credentials: GoogleCredentials

    init {
        FuelManager.instance.basePath = System.getenv("FIREBASE_URL")
        credentials = GoogleCredentials.getApplicationDefault().createScoped(
            listOf(
                "https://www.googleapis.com/auth/userinfo.email",
                "https://www.googleapis.com/auth/firebase.database"
            )
        )
    }

    fun getLastMessage(): Message? {
        val (_, _, result) = "/lastMessage.json"
            .httpGet(listOf(accessTokenParam()))
            .responseObject<Message>()

        return getResponse(result)
    }

    @ExperimentalSerializationApi
    fun updateLastMessage(message: Message): Message? {
        val (_, _, result) = "/lastMessage.json"
            .httpPut(listOf(accessTokenParam()))
            .body(Json.encodeToString(message))
            .responseObject<Message>()

        return getResponse(result)
    }

    private fun accessTokenParam() = ACCESS_TOKEN_PARAM to credentials.refreshAccessToken().tokenValue

    private fun getResponse(result: Result<Message, FuelError>) =
        when (result) {
            is Success -> result.get()
            is Failure -> {
                println(result.getException())
                null
            }
        }
}
