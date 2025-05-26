package telegram

import trainer.LearnWordsTrainer

const val START_COMMAND = "/start"
const val TIMER_SLEEP = 2000L

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId = 0L
    val trainer = LearnWordsTrainer()

    val telegramBotService = TelegramBotService(botToken)

    while (true) {
        Thread.sleep(TIMER_SLEEP)

        val update = telegramBotService.getLastUpdateId(lastUpdateId)
        val firstUpdate = update.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val message = firstUpdate.message?.text
        val chatId =
            firstUpdate.message?.chat?.id
                ?: firstUpdate.callbackQuery?.message?.chat?.id
                ?: continue

        val data = firstUpdate.callbackQuery?.data

        when {
            message?.lowercase() == START_COMMAND -> telegramBotService.sendMenu(chatId)
            data?.lowercase() == STATISTICS_CLICKED -> {
                val infoStatistics = trainer.getStatistics()
                telegramBotService
                    .sendMessage(
                        "Выучено ${infoStatistics.count} из ${infoStatistics.totalCount} | ${infoStatistics.percent} %",
                        chatId
                    )
            }

            data?.lowercase() == LEARN_WORDS_CLICKED -> {
                telegramBotService.checkNextQuestionAndSend(
                    trainer,
                    telegramBotService,
                    chatId
                )
            }

            data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                val answerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toIntOrNull()
                val correctAnswerResult = trainer.question?.correctAnswer

                if (trainer.checkAnswer(answerIndex)) {
                    telegramBotService.sendMessage("Правильно!", chatId)
                } else telegramBotService.sendMessage(
                    "Неправильно! ${correctAnswerResult?.original} – это ${correctAnswerResult?.translate}",
                    chatId
                )

                telegramBotService.checkNextQuestionAndSend(
                    trainer,
                    telegramBotService,
                    chatId
                )
            }
        }
    }
}