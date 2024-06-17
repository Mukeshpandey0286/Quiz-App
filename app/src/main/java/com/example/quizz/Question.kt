package com.example.quizz

data class Question(
    val question: String,
    val options: Map<String, String>,
    val answer: String // This should be a string representing the correct option key
)

