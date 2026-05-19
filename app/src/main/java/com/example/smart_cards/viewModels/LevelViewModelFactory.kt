package com.example.smart_cards.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smart_cards.repository.LevelRepository
import com.example.smart_cards.usecases.CheckAnswerUseCase
import com.example.smart_cards.usecases.GenerateGameQuestionsUseCase
import com.example.smart_cards.usecases.GetLevelsUseCase
import com.example.smart_cards.usecases.NextQuestionUseCase
import com.example.smart_cards.usecases.StartLevelUseCase

@Suppress("UNCHECKED_CAST")
class LevelViewModelFactory(
    private val repository: LevelRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LevelViewModel::class.java)) {
            val getLevelsUseCase = GetLevelsUseCase(repository)
            val generateQuestionsUseCase = GenerateGameQuestionsUseCase()
            val startLevelUseCase = StartLevelUseCase(repository, generateQuestionsUseCase)
            val checkAnswerUseCase = CheckAnswerUseCase()
            val nextQuestionUseCase = NextQuestionUseCase()

            return LevelViewModel(
                getLevelsUseCase = getLevelsUseCase,
                startLevelUseCase = startLevelUseCase,
                checkAnswerUseCase = checkAnswerUseCase,
                nextQuestionUseCase = nextQuestionUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}