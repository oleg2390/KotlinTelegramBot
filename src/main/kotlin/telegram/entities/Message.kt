package telegram.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import telegram.entities.Chat

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)