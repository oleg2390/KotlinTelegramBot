package org.example

import java.io.File

private const val LEARNED_COUNT = 3
private const val PERCENT_100 = 100
private const val NUMBER_WORD_VARIANT = 4
private const val EXIT_NUMBER_CODE = 0
private const val ANSWER_INDEX_CORRECTION = 1

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun main() {

    val dictionary = loadDictionary()

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
                    val notLearnedList = dictionary.filter { it.correctAnswersCount < LEARNED_COUNT }

                    if (notLearnedList.isEmpty()) {
                        println("Все слова в словаре выучены")
                        break
                    } else {

                        val questionWords = notLearnedList.shuffled().take(NUMBER_WORD_VARIANT)
                        val missingCount = NUMBER_WORD_VARIANT - questionWords.size

                        val additionalWords = if (missingCount < NUMBER_WORD_VARIANT) {
                            dictionary
                                .filterNot { it in questionWords }
                                .shuffled()
                                .take(NUMBER_WORD_VARIANT - questionWords.size)
                        } else {
                            emptyList()
                        }

                        val resultWord = (questionWords + additionalWords).shuffled()
                        val correctAnswer = notLearnedList.random()

                        println()
                        println("${correctAnswer.original}:")
                        resultWord.forEachIndexed() { index, word ->
                            println(" ${index + ANSWER_INDEX_CORRECTION} - ${word.translate}")
                        }
                        println(" ----------\n 0 - Меню")

                        val userAnswerInput = readlnOrNull()?.toIntOrNull()

                        when {
                            userAnswerInput == EXIT_NUMBER_CODE -> break
                            userAnswerInput != null && userAnswerInput in ANSWER_INDEX_CORRECTION..resultWord.size -> {

                                val selectWord = resultWord[userAnswerInput - ANSWER_INDEX_CORRECTION]

                                if (selectWord.original == correctAnswer.original) {
                                    println("Правильно")
                                    correctAnswer.correctAnswersCount++

                                    dictionary.map { word ->
                                        if (word.original == correctAnswer.original) {
                                            word.correctAnswersCount = correctAnswer.correctAnswersCount
                                        }
                                    }
                                    saveDictionary(dictionary)
                                } else println("не привильно, ответ: ${correctAnswer.translate}")
                            }

                            else -> println("введите корректное число")
                        }
                    }
                }
            }

            2 -> {
                val count = dictionary.filter { it.correctAnswersCount >= LEARNED_COUNT }.count()
                val totalCount = dictionary.size
                val percent = count * PERCENT_100 / totalCount
                println("Выучено $count из $totalCount | $percent%\n")
            }

            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}

fun loadDictionary(): List<Word> {

    val wordsFile: File = File("words.txt")

    return wordsFile.readLines().map { line ->
        val line = line.split("|")
        val correctCount = line.getOrNull(2)?.toIntOrNull() ?: 0
        Word(original = line[0], translate = line[1], correctAnswersCount = correctCount)
    }
}

fun saveDictionary(dictionary: List<Word>) {

    val content = dictionary.joinToString("\n") { "${it.original}|${it.translate}|${it.correctAnswersCount}" }
    File("words.txt").writeText(content)
}