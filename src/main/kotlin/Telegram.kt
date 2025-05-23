import org.example.LearnWordsTrainer

const val HTTP_URL = "https://api.telegram.org/bot"
const val START_COMMAND = "/start"

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0
    val trainer = LearnWordsTrainer()

    val updateIdRegex: Regex = "\"update_id\":(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = "\"chat\":\\{\"id\":(\\d+)".toRegex()

    while (true) {
        Thread.sleep(2000)
        val telegramBotService = TelegramBotService(botToken)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        val lastUpdateId =
            updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        updateId = lastUpdateId + 1

        val matchResult = messageTextRegex.findAll(updates).toList()
        val text = matchResult.lastOrNull()?.groups?.get(1)?.value.toString()
        val data = dataRegex.find(updates)?.groups?.get(1)?.value
        val chatIdResult: Long = chatIdRegex
            .find(updates)?.groups?.get(1)?.value?.toLongOrNull() ?: continue

        when {
            text.lowercase() == START_COMMAND -> telegramBotService.sendMenu(chatIdResult)
            data?.lowercase() == STATISTICS_CLICKED -> {
                val infoStatistics = trainer.getStatistics()
                telegramBotService
                    .sendMessage(
                        "Выучено ${infoStatistics.count} из ${infoStatistics.totalCount} | ${infoStatistics.percent} %",
                        chatIdResult
                    )
            }

            data?.lowercase() == LEARN_WORDS_CLICKED -> {
                telegramBotService.checkNextQuestionAndSend(
                    trainer,
                    telegramBotService,
                    chatIdResult
                )
            }

            data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                val answerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toIntOrNull()
                val correctAnswerResult = trainer.question?.correctAnswer

                if (trainer.checkAnswer(answerIndex)) {
                    telegramBotService.sendMessage("Правильно!", chatIdResult)

                } else telegramBotService.sendMessage(
                    "Неправильно! ${correctAnswerResult?.original} – это ${correctAnswerResult?.translate}",
                    chatIdResult
                )

                telegramBotService.checkNextQuestionAndSend(
                    trainer,
                    telegramBotService,
                    chatIdResult
                )
            }
        }
    }
}