package com.example.quizz

import android.util.Log
import androidx.lifecycle.ViewModel

class QuizViewModel : ViewModel() {
    private var questions: List<Question> = listOf()
    private var currentQuestionIndex: Int = 0
    private var score: Int = 0

    fun setQuestions(questions: List<Question>) {
        this.questions = questions
    }

    fun getCurrentQuestion(): Question {
        if (questions.isEmpty()) {
            throw IllegalStateException("Questions list is empty")
        }
        return questions[currentQuestionIndex]
    }

    fun submitAnswer(answer: String) {
        val currentQuestion = questions[currentQuestionIndex]
        val correctAnswer = currentQuestion.answer
        // Extract the selected option text, excluding the key (e.g., "a. ")
        val selectedAnswer = answer.substringAfter(". ")
        if (correctAnswer == selectedAnswer) {
            score++
            Log.d("QuizViewModel", "Correct Answer! Score: $score")
        } else {
            Log.d("QuizViewModel", "Wrong Answer. Correct: $correctAnswer, Selected: $selectedAnswer")
        }
    }


    fun isQuizFinished(): Boolean {
        return currentQuestionIndex >= questions.size - 1
    }

    fun setCurrentQuestionIndex(index: Int) {
        currentQuestionIndex = index
    }

    fun getCurrentQuestionIndex(): Int {
        return currentQuestionIndex
    }

    fun getScore(): Int {
        return score
    }

    fun getTotalQuestions(): Int {
        return questions.size
    }

    fun moveNextQuestion() {
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
        }
    }
}
