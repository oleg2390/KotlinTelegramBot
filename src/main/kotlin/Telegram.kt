import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0
    val updateIdRegex: Regex = "\"update_id\":\\s*(\\d+)".toRegex()

    while (true) {
        Thread.sleep(2000)
        val telegramBotService = TelegramBotService()
        val updates: String = telegramBotService.getUpdates(botToken, updateId)
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

        val sendRegexChat: Regex = "\"chat\":\\{\"id\":(-?\\d+)".toRegex()
        val matchResultChat = sendRegexChat.find(updates)
        val groupsChat = matchResultChat?.groups
        val chatIdResult = groupsChat?.get(1)?.value?.toLong()

        telegramBotService.sendMessage(botToken, text, chatIdResult)
    }
}

class TelegramBotService {

    fun getUpdates(token: String, updateId: Int): String {
        val urlGetUpdates = "https://api.telegram.org/bot$token/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(token: String, text: String, chatId: Long?): String {

        when {
            text.isBlank() -> return "Ошибка, текст не может быть пустым"
            chatId == null -> return "Ошибка, chatId не указан"
        }

        val limitsText = if (text.length > 4096) {
            text.substring(0, 4096)
        } else text

        val urlGetChat = "https://api.telegram.org/bot$token/sendMessage"
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