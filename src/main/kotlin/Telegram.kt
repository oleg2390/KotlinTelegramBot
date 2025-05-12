import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val HTTP_URL = "https://api.telegram.org/bot"

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0
    val updateIdRegex: Regex = "\"update_id\":\\s*(\\d+)".toRegex()

    while (true) {
        Thread.sleep(2000)
        val telegramBotService = TelegramBotService(botToken)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        val matches = updateIdRegex.findAll(updates)
        if (matches.any()) {
            for (match in matches) {
                val id = match.groupValues[1].toInt()
                if (id >= updateId) {
                    updateId + 1
                }
            }
        }

        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult = messageTextRegex.findAll(updates).toList()
        val text = matchResult.lastOrNull()?.groups?.get(1)?.value.toString()
        println(text)

        val chatIdResult: Long = "\"chat\":\\{\"id\":(-?\\d+)".toRegex()
            .find(updates)
            ?.groups?.get(1)?.value?.toLongOrNull()
            ?: throw IllegalArgumentException("Ошибка, chat_id не найден")

        telegramBotService.sendMessage(text, chatIdResult)
    }
}

class TelegramBotService(
    private val token: String,
) {

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$HTTP_URL$token/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(text: String, chatId: Long): String {

        if (text.isBlank()) return "Ошибка, текст не может быть пустым"

        val limitsText = if (text.length > 4096) {
            text.substring(0, 4096)
        } else text

        val urlGetChat = "$HTTP_URL$token/sendMessage"
        val clientChat = HttpClient.newBuilder().build()

        val jsonBody = """
        {
            "chat_id": $chatId,
            "text": "$limitsText"
        }
    """.trimIndent()

        val requestChat = HttpRequest.newBuilder()
            .uri(URI.create(urlGetChat))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build()

        val responseChat = clientChat.send(requestChat, HttpResponse.BodyHandlers.ofString())
        return responseChat.body()
    }
}