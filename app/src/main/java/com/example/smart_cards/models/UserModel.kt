package com.example.smart_cards.models

import androidx.lifecycle.ViewModel

class UserModel : ViewModel() {
    var userName: String = "Иван Петров"
    var userLevel: Int = 5
    var streakDays: Int = 7
    var experience: Int = 1250
}