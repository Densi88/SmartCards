package com.example.smart_cards.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.smart_cards.R
import com.example.smart_cards.db.dbAdapter
import com.example.smart_cards.viewModels.LevelViewModelFactory
import com.example.smart_cards.repository.LevelRepository
import com.example.smart_cards.viewModels.LevelViewModel
import kotlinx.coroutines.launch

class LevelFragment : Fragment() {

    private lateinit var tvLevel: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvWord: TextView
    private lateinit var buttonsContainer: LinearLayout
    private lateinit var btnNextLevel: Button
    private lateinit var btnRestart: Button
    private lateinit var btnBack: Button

    private val viewModel: LevelViewModel by viewModels {
        LevelViewModelFactory(LevelRepository(dbAdapter))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.level_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        tvLevel = view.findViewById(R.id.tvLevel)
        tvProgress = view.findViewById(R.id.tvProgress)
        tvScore = view.findViewById(R.id.tvScore)
        tvWord = view.findViewById(R.id.tvWord)
        buttonsContainer = view.findViewById(R.id.buttonsContainer)
        btnNextLevel = view.findViewById(R.id.btnNextLevel)
        btnRestart = view.findViewById(R.id.btnRestart)
        btnBack = view.findViewById(R.id.btnBack)
    }

    private fun setupClickListeners() {
        btnNextLevel.setOnClickListener {
            // Показать следующий уровень (логика в activity или навигация)
            // Можно отправить результат в activity
            parentFragmentManager.popBackStack()
        }

        btnRestart.setOnClickListener {
            viewModel.restartGame()
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is LevelViewModel.GameUiState.Loading -> {
                        showLoading()
                    }
                    is LevelViewModel.GameUiState.LevelSelection -> {
                        showLevelSelection(state.levels)
                    }
                    is LevelViewModel.GameUiState.Question -> {
                        showQuestion(state)
                    }
                    is LevelViewModel.GameUiState.GameComplete -> {
                        showGameComplete(state)
                    }
                    is LevelViewModel.GameUiState.Error -> {
                        showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        tvWord.text = "Загрузка..."
        buttonsContainer.removeAllViews()
        btnNextLevel.visibility = Button.GONE
        btnRestart.visibility = Button.GONE
    }

    private fun showLevelSelection(levels: List<Int>) {
        buttonsContainer.removeAllViews()
        tvLevel.text = "Выберите уровень"
        tvProgress.text = ""
        tvScore.text = ""

        levels.forEach { level ->
            val button = Button(requireContext())
            button.text = "Уровень $level"
            button.setTextAppearance(android.R.style.TextAppearance_Material_Button)
            button.setPadding(0, 32, 0, 32)

            // Стилизация кнопки
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 16, 0, 16)
            button.layoutParams = params

            button.setOnClickListener {
                viewModel.startLevel(level)
            }
            buttonsContainer.addView(button)
        }

        tvWord.text = "Нажмите на уровень, чтобы начать игру"
        btnNextLevel.visibility = Button.GONE
        btnRestart.visibility = Button.GONE
        btnBack.visibility = Button.VISIBLE
    }

    private fun showQuestion(state: LevelViewModel.GameUiState.Question) {
        tvLevel.text = "Уровень ${state.level}"
        tvProgress.text = "Вопрос ${state.progress}/${state.question.options.size}"
        tvScore.text = "Правильно: ${state.score}"
        tvWord.text = state.question.card.word

        buttonsContainer.removeAllViews()

        state.question.options.forEach { option ->
            val button = Button(requireContext())
            button.text = option
            button.setTextAppearance(android.R.style.TextAppearance_Material_Button)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 8, 0, 8)
            button.layoutParams = params

            button.setOnClickListener {
                // Блокируем кнопки на секунду, чтобы не было двойного нажатия
                button.isEnabled = false
                viewModel.answerQuestion(option)

                // Показываем визуальную обратную связь
                val isCorrect = option == state.question.correctAnswer
                if (isCorrect) {
                    button.setBackgroundColor(Color.parseColor("#4CAF50"))
                } else {
                    button.setBackgroundColor(Color.parseColor("#F44336"))
                    // Находим и подсвечиваем правильный ответ
                    for (i in 0 until buttonsContainer.childCount) {
                        val child = buttonsContainer.getChildAt(i) as? Button
                        if (child?.text == state.question.correctAnswer) {
                            child.setBackgroundColor(Color.parseColor("#4CAF50"))
                        }
                    }
                }

                // Возвращаем цвета через 1 секунду и разблокируем
                button.postDelayed({
                    resetButtonColors()
                    enableAllButtons(true)
                }, 1000)
            }

            buttonsContainer.addView(button)
        }

        btnNextLevel.visibility = Button.GONE
        btnRestart.visibility = Button.GONE
        btnBack.visibility = Button.GONE
    }

    private fun resetButtonColors() {
        for (i in 0 until buttonsContainer.childCount) {
            val button = buttonsContainer.getChildAt(i) as? Button
            button?.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun enableAllButtons(enabled: Boolean) {
        for (i in 0 until buttonsContainer.childCount) {
            val button = buttonsContainer.getChildAt(i) as? Button
            button?.isEnabled = enabled
        }
    }

    private fun showGameComplete(state: LevelViewModel.GameUiState.GameComplete) {
        val message = buildString {
            appendLine("Уровень ${state.level} пройден!")
            appendLine()
            appendLine("Результат: ${state.score} из ${state.total}")
            appendLine()
            if (state.score >= state.total / 2) {
                appendLine("Поздравляю!")
                appendLine()
                appendLine("Вы можете перейти на следующий уровень")
            } else {
                appendLine("Попробуй еще раз!")
                appendLine()
                appendLine("Нужно ответить правильно хотя бы на половину вопросов")
            }
        }

        tvWord.text = message
        buttonsContainer.removeAllViews()

        btnNextLevel.visibility = if (state.score >= state.total / 2) {
            Button.VISIBLE
        } else {
            Button.GONE
        }

        btnRestart.visibility = Button.VISIBLE
        btnBack.visibility = Button.VISIBLE
    }

    private fun showError(message: String) {
        tvWord.text = "Ошибка: $message"
        buttonsContainer.removeAllViews()

        val retryButton = Button(requireContext())
        retryButton.text = "Попробовать снова"
        retryButton.setOnClickListener {
            viewModel.restartGame()
        }
        buttonsContainer.addView(retryButton)

        btnBack.visibility = Button.VISIBLE
        btnNextLevel.visibility = Button.GONE
        btnRestart.visibility = Button.GONE
    }
}