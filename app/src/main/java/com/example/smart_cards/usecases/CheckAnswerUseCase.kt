package com.example.smart_cards.usecases

class CheckAnswerUseCase {

    operator fun invoke(selectedAnswer: String, correctAnswer: String): Boolean {
        return selectedAnswer == correctAnswer
    }
}