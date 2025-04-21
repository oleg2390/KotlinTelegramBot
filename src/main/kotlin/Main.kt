package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun main() {

    val wordsFile: File = File("words.txt")
    var dictionary = mutableListOf<Word>()

    val lines = wordsFile.readLines()
    for (line in lines) {
        val line = line.split("|")
        val correctCount = line.getOrNull(2)?.toIntOrNull() ?: 0
        val word = Word(original = line[0], translate = line[1], correctAnswersCount = correctCount)
        dictionary.add(word)
        println(word)
    }
    dictionary.forEach { println("${it.original} - ${it.translate} - ${it.correctAnswersCount}") }
}