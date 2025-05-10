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
        val updates: String = getUpdates(botToken, updateId)
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
        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value
        println(text)
    }
}

fun getUpdates(token: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$token/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}
