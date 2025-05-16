import org.example.LearnWordsTrainer
import org.example.Question
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val STATISTICS_CLICKED = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

class TelegramBotService(
    private val token: String,
    private val httpClient: HttpClient = HttpClient.newBuilder().build(),
) {

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$HTTP_URL$token/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(text: String, chatId: Long): String {

        if (text.isBlank()) return "Ошибка, текст не может быть пустым"

        val limitsText = if (text.length > 4096) {
            text.substring(0, 4096)
        } else text

        val urlGetChat = "$HTTP_URL$token/sendMessage"

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

        val responseChat = httpClient.send(requestChat, HttpResponse.BodyHandlers.ofString())
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
                                "callback_data": "$LEARN_WORDS_CLICKED"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "$STATISTICS_CLICKED"
                            }
                        ]
                    ]
                }
            }
    """.trimIndent()

        val requestChat = HttpRequest.newBuilder()
            .uri(URI.create(urlGetChat))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()

        val responseChat = httpClient.send(requestChat, HttpResponse.BodyHandlers.ofString())
        return responseChat.body()
    }

    fun checkNextQuestionAndSend(

        trainer: LearnWordsTrainer,
        telegramBotService: TelegramBotService,
        chatId: Long,
    ) {

        val question = trainer.getNextQuestion()

        if (question == null) {
            telegramBotService.sendMessage("Все слова в словаре выучены", chatId)
        } else telegramBotService.sendQuestion(chatId, question)
    }

    private fun sendQuestion(chatId: Long, question: Question?): String {

        val urlGetChat = "$HTTP_URL$token/sendMessage"

        val inlineKeyboard = question?.variant?.mapIndexed { index, word ->
            mapOf(
                "text" to word.translate,
                "callback_data" to "$CALLBACK_DATA_ANSWER_PREFIX$index"
            )
        }?.chunked(2)

        val jsonBody = """
        {
            "chat_id": $chatId,
            "text": "${question?.correctAnswer?.original}",
            "reply_markup": {
                "inline_keyboard": [
                    ${
            inlineKeyboard?.joinToString { row ->
                row.joinToString(",", prefix = "[", postfix = "]") {
                    "{\"text\":\"${it["text"]}\",\"callback_data\":\"${it["callback_data"]}\"}"
                }
            }
        }
                ]
            }
        }
    """.trimIndent()

        val requestChat = HttpRequest.newBuilder()
            .uri(URI.create(urlGetChat))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build()

        val responseChat = httpClient.send(requestChat, HttpResponse.BodyHandlers.ofString())
        return responseChat.body()
    }
}