import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.LearnWordsTrainer

const val HTTP_URL = "https://api.telegram.org/bot"
const val START_COMMAND = "/start"

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId = 0L
    val trainer = LearnWordsTrainer()

    val json = Json {
        ignoreUnknownKeys = true
    }

    while (true) {
        Thread.sleep(2000)
        val telegramBotService = TelegramBotService(botToken)
        val responseString: String = telegramBotService.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = json.decodeFromString(responseString)
        val update = response.result
        val firstUpdate = update.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val message = firstUpdate.message?.text
        val chatIdResult =
            firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val data = firstUpdate.callbackQuery?.data

        when {
            message?.lowercase() == START_COMMAND -> chatIdResult?.let {
                telegramBotService.sendMenu(json, it)
            }

            data?.lowercase() == STATISTICS_CLICKED -> {
                val infoStatistics = trainer.getStatistics()
                if (chatIdResult != null) {
                    telegramBotService
                        .sendMessage(
                            json,
                            "Выучено ${infoStatistics.count} из ${infoStatistics.totalCount} | ${infoStatistics.percent} %",
                            chatIdResult
                        )
                }
            }

            data?.lowercase() == LEARN_WORDS_CLICKED -> {
                if (chatIdResult != null) {
                    telegramBotService.checkNextQuestionAndSend(
                        json,
                        trainer,
                        telegramBotService,
                        chatIdResult
                    )
                }
            }

            data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                val answerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toIntOrNull()
                val correctAnswerResult = trainer.question?.correctAnswer

                if (trainer.checkAnswer(answerIndex)) {
                    chatIdResult?.let {
                        telegramBotService.sendMessage(json, "Правильно!", it)
                    }

                } else chatIdResult?.let {
                    telegramBotService.sendMessage(
                        json,
                        "Неправильно! ${correctAnswerResult?.original} – это ${correctAnswerResult?.translate}",
                        it
                    )
                }

                if (chatIdResult != null) {
                    telegramBotService.checkNextQuestionAndSend(
                        json,
                        trainer,
                        telegramBotService,
                        chatIdResult
                    )
                }
            }
        }
    }
}