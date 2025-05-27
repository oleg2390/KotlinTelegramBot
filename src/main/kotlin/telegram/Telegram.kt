package telegram

import trainer.LearnWordsTrainer

const val START_COMMAND = "/start"
const val TIMER_SLEEP = 2000L

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId = 0L
    val trainers = HashMap<Long, LearnWordsTrainer>()
    val telegramBotService = TelegramBotService(botToken)

    while (true) {
        Thread.sleep(TIMER_SLEEP)

        if (telegramBotService.getLastUpdateId(lastUpdateId).isEmpty()) continue
        val sortedUpdates =
            telegramBotService.getLastUpdateId(lastUpdateId).sortedBy { it.updateId }
        sortedUpdates.forEach { telegramBotService.handleUpdate(it, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}