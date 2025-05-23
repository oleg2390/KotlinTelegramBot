package org.example

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

data class Statistics(
    val count: Int,
    val totalCount: Int,
    val percent: Int,
)

data class Question(
    val variant: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    val learnedCount: Int = 3,
    val numberWordVariants: Int = 4,
) {

    var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {

        val count = dictionary.count { it.correctAnswersCount >= learnedCount }
        val totalCount = dictionary.size
        val percent = count * PERCENT_100 / totalCount
        return Statistics(count, totalCount, percent)
    }

    fun getNextQuestion(): Question? {

        val notLearnedList = dictionary.filter { it.correctAnswersCount < learnedCount }

        if (notLearnedList.isEmpty()) return null
        val questionWords = notLearnedList.shuffled().take(numberWordVariants)
        val missingCount = numberWordVariants - questionWords.size

        val additionalWords = if (missingCount < numberWordVariants) {
            dictionary
                .filterNot { it in questionWords }
                .shuffled()
                .take(numberWordVariants - questionWords.size)
        } else emptyList()

        val resultWord = (questionWords + additionalWords).shuffled()
        val correctAnswer = questionWords.random()
        question = Question(
            variant = resultWord,
            correctAnswer = correctAnswer
        )
        return question
    }

    fun checkAnswer(correctAnswerIndex: Int?): Boolean {

        return question?.let {
            val selectWord = question?.variant?.indexOf(it.correctAnswer)
            if (selectWord == correctAnswerIndex) {
                it.correctAnswer.correctAnswersCount++

                saveDictionary()
                true
            } else false
        } ?: false
    }

    private fun loadDictionary(): List<Word> {

        try {
            val wordsFile: File = File("words.txt")
            return wordsFile.readLines().map { line ->
                val line = line.split("|")
                val correctCount = line.getOrNull(2)?.toIntOrNull() ?: 0
                Word(original = line[0], translate = line[1], correctAnswersCount = correctCount)
            }
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("некорректный файл")
        }
    }

    private fun saveDictionary() {

        val words = File("words.txt")
        words.writeText("")
        dictionary.forEach {
            words.appendText("${it.original}|${it.translate}|${it.correctAnswersCount}\n")
        }
    }
}