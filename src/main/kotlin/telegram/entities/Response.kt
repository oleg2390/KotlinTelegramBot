package telegram.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import telegram.entities.Update

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)