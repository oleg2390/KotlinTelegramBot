package trainer.model

import kotlinx.serialization.Serializable

@Serializable
data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)