import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

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

        println(text)
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

    fun sendMenu(chatId: Long): String {

        val urlGetChat = "$HTTP_URL$token/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова",
                                "callback_data": "lear_words_clicked"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "statistics_clicked"
                            }
                        ]
                    ]
                }
            }
    """.trimIndent()

        val clientChat = HttpClient.newBuilder().build()
        val requestChat = HttpRequest.newBuilder()
            .uri(URI.create(urlGetChat))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()

        val responseChat = clientChat.send(requestChat, HttpResponse.BodyHandlers.ofString())
        return responseChat.body()
    }
}