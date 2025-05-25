package telegram

import kotlinx.serialization.json.Json
import telegram.entities.InlineKeyboard
import telegram.entities.ReplyMarkup
import telegram.entities.Response
import telegram.entities.SendMessageRequest
import telegram.entities.Update
import trainer.LearnWordsTrainer
import trainer.model.Question
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
    private val json: Json = Json {
        ignoreUnknownKeys = true
    }
) {
    companion object {
        const val HTTP_URL = "https://api.telegram.org/bot"
    }

    fun getLastUpdateId(lastUpdateId: Long): List<Update> {

        val responseString: String = getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = json.decodeFromString(responseString)
        return response.result
    }

    fun getUpdates(updateId: Long): String {

        val urlGetUpdates = "$HTTP_URL$token/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> =
            httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(text: String, chatId: Long): String {

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

    fun sendMenu(chatId: Long): String {

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

        trainer: LearnWordsTrainer,
        telegramBotService: TelegramBotService,
        chatId: Long,
    ) {

        val question = trainer.getNextQuestion()

        if (question == null) {
            telegramBotService.sendMessage("Все слова в словаре выучены", chatId)
        } else telegramBotService.sendQuestion(chatId, question)
    }

    private fun sendQuestion(chatId: Long, question: Question): String {

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