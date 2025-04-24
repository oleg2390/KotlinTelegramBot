package org.example

import java.io.File

private const val LEARNED_COUNT = 3
private const val PERCENT_100 = 100
private const val NUMBER_4 = 4

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun main() {

    val dictionary = loadDictionary()
    val notLearnedList = dictionary.filter { it.correctAnswersCount < LEARNED_COUNT }

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
                if (notLearnedList.isEmpty()) {
                    println("Все слова в словаре выучены")
                    continue
                }
                while (true) {

                    val questionWords = notLearnedList.take(NUMBER_4).shuffled()
                    val correctAnswer = notLearnedList.random()

                    println()
                    println("${correctAnswer.original}:")
                    questionWords.forEachIndexed() { index, word ->
                        println(" ${index + 1} - ${word.translate}")
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
