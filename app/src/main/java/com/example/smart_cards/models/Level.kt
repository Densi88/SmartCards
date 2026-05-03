package com.example.smart_cards.models

data class Level(val number: String, val cards: List<Card> = emptyList()) {

}
data class GameSession(
    val levelNumber: Int,
    val cards: List<Card>,
    val currentCardIndex: Int = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0
){

}

data class GameQuestion(
    val card: Card,
    val options: List<String>,  // варианты ответов
    val correctAnswer: String
)