package com.example.smart_cards.usecases

import com.example.smart_cards.models.Card
import com.example.smart_cards.models.GameQuestion

class GenerateGameQuestionsUseCase {

    operator fun invoke(cards: List<Card>): List<GameQuestion> {
        val questions = mutableListOf<GameQuestion>()

        cards.forEach { card ->
            val otherTranslations = cards
                .filter { it.word != card.word }
                .map { it.translate }
                .distinct()
                .shuffled()
                .take(3)

            val options = (otherTranslations + card.translate).shuffled()

            questions.add(
                GameQuestion(
                    card = card,
                    options = options,
                    correctAnswer = card.translate
                )
            )
        }

        return questions.shuffled()
    }
}