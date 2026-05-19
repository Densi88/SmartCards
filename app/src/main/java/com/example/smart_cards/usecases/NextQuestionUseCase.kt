package com.example.smart_cards.usecases

import com.example.smart_cards.models.GameQuestion
import com.example.smart_cards.models.GameSession
import com.example.smart_cards.viewModels.LevelViewModel.GameUiState

class NextQuestionUseCase {
    operator fun invoke(
        session: GameSession,
        questions: List<GameQuestion>
    ): GameUiState {
        return if (session.currentCardIndex >= questions.size) {
            GameUiState.GameComplete(
                score = session.correctAnswers,
                total = session.totalQuestions,
                level = session.levelNumber
            )
        } else {
            val currentQuestion = questions[session.currentCardIndex]
            GameUiState.Question(
                question = currentQuestion,
                progress = session.currentCardIndex + 1,
                score = session.correctAnswers,
                level = session.levelNumber
            )
        }
    }
}