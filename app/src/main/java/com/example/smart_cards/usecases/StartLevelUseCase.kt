package com.example.smart_cards.usecases

import com.example.smart_cards.models.Card
import com.example.smart_cards.models.GameSession
import com.example.smart_cards.repository.LevelRepository
import com.example.smart_cards.models.GameQuestion

class StartLevelUseCase(
    private val repository: LevelRepository,
    private val generateQuestionsUseCase: GenerateGameQuestionsUseCase
) {

    suspend operator fun invoke(levelNumber: Int): StartLevelResult {
        val cards = repository.getCardsForLevel(levelNumber)

        if (cards.isEmpty()) {
            return StartLevelResult.Error("На этом уровне нет карточек")
        }

        val session = GameSession(
            levelNumber = levelNumber,
            cards = cards,
            totalQuestions = cards.size
        )

        val questions = generateQuestionsUseCase(cards)

        return StartLevelResult.Success(session, questions)
    }

    sealed class StartLevelResult {
        data class Success(val session: GameSession, val questions: List<GameQuestion>) : StartLevelResult()
        data class Error(val message: String) : StartLevelResult()
    }
}