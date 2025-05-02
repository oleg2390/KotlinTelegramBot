package org.example

import java.io.File

data class Statistics(
    val count: Int,
    val totalCount: Int,
    val percent: Int,
)

data class Question(
    val variant: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer {

    private var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {

        val count = dictionary.filter { it.correctAnswersCount >= LEARNED_COUNT }.count()
        val totalCount = dictionary.size
        val percent = count * PERCENT_100 / totalCount
        return Statistics(count, totalCount, percent)
    }

    fun getNextQuestion(): Question? {

        val notLearnedList = dictionary.filter { it.correctAnswersCount < LEARNED_COUNT }

        if (notLearnedList.isEmpty()) return null
        val questionWords = notLearnedList.shuffled().take(NUMBER_WORD_VARIANT)
        val missingCount = NUMBER_WORD_VARIANT - questionWords.size

        val additionalWords = if (missingCount < NUMBER_WORD_VARIANT) {
            dictionary
                .filterNot { it in questionWords }
                .shuffled()
                .take(NUMBER_WORD_VARIANT - questionWords.size)
        } else emptyList()

        val resultWord = (questionWords + additionalWords).shuffled()
        val correctAnswer = notLearnedList.random()
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

                saveDictionary(dictionary)
                true
            } else false
        } ?: false
    }

    private fun loadDictionary(): List<Word> {

        val wordsFile: File = File("words.txt")
        return wordsFile.readLines().map { line ->
            val line = line.split("|")
            val correctCount = line.getOrNull(2)?.toIntOrNull() ?: 0
            Word(original = line[0], translate = line[1], correctAnswersCount = correctCount)
        }
    }

    private fun saveDictionary(dictionary: List<Word>) {

        val content = dictionary.joinToString("\n") { "${it.original}|${it.translate}|${it.correctAnswersCount}" }
        File("words.txt").writeText(content)
    }
}


