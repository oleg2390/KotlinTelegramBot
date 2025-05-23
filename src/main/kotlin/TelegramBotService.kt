import kotlinx.serialization.json.Json
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

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$HTTP_URL$token/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(json: Json, text: String, chatId: Long): String {

        if (text.isBlank()) return "Ошибка, текст не может быть пустым"

        val limitsText = if (text.length > 4096) {
            text.substring(0, 4096)
        } else text

        val urlGetChat = "$HTTP_URL$token/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = limitsText,
        )

        val requestBodyString = json.encodeToString(requestBody)

        val requestChat = HttpRequest.newBuilder()
            .uri(URI.create(urlGetChat))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val responseChat = httpClient.send(requestChat, HttpResponse.BodyHandlers.ofString())
        return responseChat.body()
    }

    fun sendMenu(json: Json, chatId: Long): String {

        val urlGetChat = "$HTTP_URL$token/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "Изучить слова", callbackData = LEARN_WORDS_CLICKED),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED)
                    )
                )
            )
        )

        val requestBodyString = json.encodeToString(requestBody)

        val requestChat = HttpRequest.newBuilder()
            .uri(URI.create(urlGetChat))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val responseChat = httpClient.send(requestChat, HttpResponse.BodyHandlers.ofString())
        return responseChat.body()
    }

    fun checkNextQuestionAndSend(

        json: Json,
        trainer: LearnWordsTrainer,
        telegramBotService: TelegramBotService,
        chatId: Long,
    ) {

        val question = trainer.getNextQuestion()

        if (question == null) {
            telegramBotService.sendMessage(json, "Все слова в словаре выучены", chatId)
        } else telegramBotService.sendQuestion(json, chatId, question)
    }

    private fun sendQuestion(json: Json, chatId: Long, question: Question): String {

        val urlGetChat = "$HTTP_URL$token/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                listOf(question.variant.mapIndexed { index, word ->
                    InlineKeyboard(
                        text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                    )
                })
            )
        )

        val requestBodyString = json.encodeToString(requestBody)

        val requestChat = HttpRequest.newBuilder()
            .uri(URI.create(urlGetChat))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val responseChat = httpClient.send(requestChat, HttpResponse.BodyHandlers.ofString())
        return responseChat.body()
    }
}