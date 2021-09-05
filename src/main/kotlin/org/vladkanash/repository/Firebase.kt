package org.vladkanash.repository

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPatch
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
private const val LAST_MESSAGE_URI = "/lastMessage.json"

@Serializable
data class Message(val text: String? = null, val date: LocalDate)

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
        val (_, _, result) = LAST_MESSAGE_URI
            .httpGet(listOf(accessTokenParam()))
            .responseObject<Message>()

        return getResponse(result)
    }

    @ExperimentalSerializationApi
    fun updateLastMessage(message: Message): Message? {
        val (_, _, result) = LAST_MESSAGE_URI
            .httpPatch(listOf(accessTokenParam()))
            .body(Json.encodeToString(message))
            .responseObject<Message>()

        return getResponse(result)
    }

    private fun accessTokenParam() = ACCESS_TOKEN_PARAM to
            credentials
                .apply { refreshIfExpired() }
                .accessToken.tokenValue

    private fun getResponse(result: Result<Message, FuelError>) =
        when (result) {
            is Success -> result.get()
            is Failure -> {
                println(result.getException())
                null
            }
        }
}
