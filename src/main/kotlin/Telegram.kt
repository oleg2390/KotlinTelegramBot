import org.example.LearnWordsTrainer

const val HTTP_URL = "https://api.telegram.org/bot"

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0
    val trainer = LearnWordsTrainer()

    val updateIdRegex: Regex = "\"update_id\":(\\d+)".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val telegramBotService = TelegramBotService(botToken)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        val lastUpdateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toIntOrNull() ?: continue
        updateId = lastUpdateId + 1

        val matchResult = messageTextRegex.findAll(updates).toList()
        val text = matchResult.lastOrNull()?.groups?.get(1)?.value.toString()
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        val chatIdResult: Long = "\"chat\":\\{\"id\":(\\d+)".toRegex()
            .find(updates)
            ?.groups?.get(1)?.value?.toLongOrNull()
            ?: throw IllegalArgumentException("Ошибка, chat_id не найден")

        telegramBotService.sendMessage(text, chatIdResult)

        if (text.lowercase() == "/start") {
            telegramBotService.sendMenu(chatIdResult)
        }

        if (data?.lowercase() == "statistics_clicked") {
            telegramBotService.sendMessage("Выучено 10 из 10 слов", chatIdResult)
        }
    }
}