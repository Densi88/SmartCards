package com.example.smart_cards.usecases

import com.example.smart_cards.repository.LevelRepository

class GetLevelsUseCase(
    private val repository: LevelRepository
) {
    suspend operator fun invoke(): List<Int> {
        return repository.getAvailableLevels()
    }
}