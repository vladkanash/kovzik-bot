package org.vladkanash.function

import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.network.serialization.GsonFactory
import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import org.vladkanash.repository.Firebase
import org.vladkanash.repository.Message

@Suppress("unused")
@ExperimentalSerializationApi
class Updater(
    private val firebase: Firebase = Firebase()
) : HttpFunction {

    private val gson = GsonFactory.createForApiClient()

    override fun service(request: HttpRequest, response: HttpResponse) {
        verifyToken(request)

        val update = request.getUpdate()
        println("Update is $update")
        handleUpdate(update)
        request.reader.close()
    }

    private fun HttpRequest.getUpdate() =
        reader.readText().let { gson.fromJson(it, Update::class.java) }

    private fun handleUpdate(update: Update) {
        val kovzikId = System.getenv("KOVZIK_ID").toLong()
        if (update.message?.from?.id == kovzikId) {
            firebase.updateLastMessage(
                Message(update.message?.text ?: "", today())
            )
            println("Last message updated")
        }
    }

    private fun verifyToken(request: HttpRequest) {
        val requestToken = request.queryParameters["token"].orEmpty().firstOrNull()
        val savedToken = System.getenv("VERIFY_TOKEN")
        if (savedToken == null || requestToken != savedToken) throw IllegalArgumentException()
    }

    private fun today() = now().toLocalDateTime(currentSystemDefault()).date
}
