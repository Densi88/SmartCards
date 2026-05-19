package com.example.smart_cards.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_cards.models.UserModel
import com.example.smart_cards.repository.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(
    private val repository: UsersRepository
) : ViewModel() {

    // Состояние UI
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Idle)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    // Состояние текущего пользователя
    private val _currentUser = MutableStateFlow<UserModel?>(null)
    val currentUser: StateFlow<UserModel?> = _currentUser.asStateFlow()

    sealed class UserUiState {
        object Idle : UserUiState()
        object Loading : UserUiState()
        data class Success(val message: String) : UserUiState()
        data class Error(val message: String) : UserUiState()
    }

    // Регистрация пользователя
    fun register(email: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading

            try {
                // Валидация
                if (email.isBlank() || username.isBlank() || password.isBlank()) {
                    _uiState.value = UserUiState.Error("Заполните все поля")
                    return@launch
                }

                if (password.length < 6) {
                    _uiState.value = UserUiState.Error("Пароль должен быть не менее 6 символов")
                    return@launch
                }

                // Хеширование пароля
                val hashedPassword = withContext(Dispatchers.IO) {
                    repository.simpleHash(password)
                }

                // Регистрация
                withContext(Dispatchers.IO) {
                    repository.register(hashedPassword, email, username)
                }

                _uiState.value = UserUiState.Success("Регистрация успешна!")

            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Ошибка регистрации")
            }
        }
    }

    // Авторизация пользователя
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading

            try {
                // Валидация
                if (email.isBlank() || password.isBlank()) {
                    _uiState.value = UserUiState.Error("Заполните все поля")
                    return@launch
                }

                // Хеширование пароля
                val hashedPassword = withContext(Dispatchers.IO) {
                    repository.simpleHash(password)
                }

                // Авторизация
                val user = withContext(Dispatchers.IO) {
                    repository.authenticateUser(email, hashedPassword)
                }

                if (user != null) {
                    _currentUser.value = user
                    _uiState.value = UserUiState.Success("Добро пожаловать, ${user.userName}!")
                } else {
                    _uiState.value = UserUiState.Error("Неверный email или пароль")
                }

            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Ошибка авторизации")
            }
        }
    }

    // Выход из системы
    fun logout() {
        _currentUser.value = null
        _uiState.value = UserUiState.Success("Вы вышли из системы")
    }

    // Сброс состояния UI (после обработки события)
    fun resetUiState() {
        _uiState.value = UserUiState.Idle
    }
}