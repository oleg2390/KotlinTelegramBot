package org.example

const val LEARNED_COUNT = 3
const val PERCENT_100 = 100
const val NUMBER_WORD_VARIANT = 4
const val EXIT_NUMBER_CODE = 0
const val ANSWER_INDEX_CORRECTION = 1

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun Question.asConsoleString(): String {
    val variants = this.variant
        .mapIndexed { index, word -> " ${index + ANSWER_INDEX_CORRECTION} - ${word.translate}" }
        .joinToString(separator = "\n")
    println()
    return this.correctAnswer.original + "\n" + variants + "\n" + " ----------\n" + " 0 - выйти в меню"
}

fun main() {

    val trainer = LearnWordsTrainer()

    while (true) {

        println(
            """
        Меню: 
        1 – Учить слова
        2 – Статистика
        0 – Выход
    """.trimIndent()
        )

        val inputUser = readln().toInt()

        when (inputUser) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println("Все слова в словаре выучены")
                        break
                    } else {
                        println(question.asConsoleString())

                        val userAnswerInput = readlnOrNull()?.toIntOrNull()

                        when {
                            userAnswerInput == EXIT_NUMBER_CODE ->  break

                            userAnswerInput != null && userAnswerInput in ANSWER_INDEX_CORRECTION..question.variant.size -> {

                                if (trainer.checkAnswer(userAnswerInput.minus(1))) {
                                    println("Правильно")
                                } else println("не привильно, ответ: ${question.correctAnswer.translate}")
                            }

                            else -> println("введите корректное число")
                        }
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println("Выучено ${statistics.count} из ${statistics.totalCount} | ${statistics.percent}%\n")
            }

            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}

