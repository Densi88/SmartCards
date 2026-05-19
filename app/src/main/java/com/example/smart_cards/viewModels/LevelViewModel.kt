package com.example.smart_cards.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_cards.models.GameQuestion
import com.example.smart_cards.models.GameSession
import com.example.smart_cards.usecases.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LevelViewModel(
    private val getLevelsUseCase: GetLevelsUseCase,
    private val startLevelUseCase: StartLevelUseCase,
    private val checkAnswerUseCase: CheckAnswerUseCase,
    private val nextQuestionUseCase: NextQuestionUseCase
) : ViewModel() {

    sealed class GameUiState {
        object Loading : GameUiState()
        data class LevelSelection(val levels: List<Int>) : GameUiState()
        data class Question(
            val question: GameQuestion,
            val progress: Int,  // текущий вопрос / всего
            val score: Int,     // правильные ответы
            val level: Int
        ) : GameUiState()
        data class GameComplete(
            val score: Int,
            val total: Int,
            val level: Int
        ) : GameUiState()
        data class Error(val message: String) : GameUiState()
    }

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState

    private var currentGameSession: GameSession? = null
    private var currentQuestions: List<GameQuestion> = emptyList()

    init {
        loadLevels()
    }

    private fun loadLevels() {
        viewModelScope.launch {
            try {
                val levels = withContext(Dispatchers.IO) {
                    getLevelsUseCase()
                }
                if (levels.isEmpty()) {
                    _uiState.value = GameUiState.Error("Нет доступных уровней")
                } else {
                    _uiState.value = GameUiState.LevelSelection(levels)
                }
            } catch (e: Exception) {
                _uiState.value = GameUiState.Error(e.message ?: "Ошибка загрузки уровней")
            }
        }
    }

    fun startLevel(levelNumber: Int) {
        viewModelScope.launch {
            _uiState.value = GameUiState.Loading

            val result = withContext(Dispatchers.IO) {
                startLevelUseCase(levelNumber)
            }

            when (result) {
                is StartLevelUseCase.StartLevelResult.Success -> {
                    currentGameSession = result.session
                    currentQuestions = result.questions
                    showNextQuestion()
                }
                is StartLevelUseCase.StartLevelResult.Error -> {
                    _uiState.value = GameUiState.Error(result.message)
                }
            }
        }
    }

    private fun showNextQuestion() {
        val session = currentGameSession ?: return
        _uiState.value = nextQuestionUseCase(session, currentQuestions)
    }

    fun answerQuestion(selectedAnswer: String) {
        val session = currentGameSession ?: return
        val currentQuestion = currentQuestions.getOrNull(session.currentCardIndex) ?: return

        val isCorrect = checkAnswerUseCase(selectedAnswer, currentQuestion.correctAnswer)

        val newSession = session.copy(
            currentCardIndex = session.currentCardIndex + 1,
            correctAnswers = if (isCorrect) session.correctAnswers + 1 else session.correctAnswers
        )

        currentGameSession = newSession
        showNextQuestion()
    }

    fun restartGame() {
        currentGameSession = null
        currentQuestions = emptyList()
        loadLevels()
    }
}