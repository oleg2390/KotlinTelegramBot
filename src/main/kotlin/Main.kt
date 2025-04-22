package org.example

import java.io.File

private const val LEARNED_COUNT = 3
private const val PERCENT_100 = 100

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
            1 -> println("Учить слова")
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
