package org.example

import java.io.File

fun main() {

    val wordsFile: File = File("words.txt")

    val lines = wordsFile.readLines()
    for (line in lines) {
        println(line)
    }
}