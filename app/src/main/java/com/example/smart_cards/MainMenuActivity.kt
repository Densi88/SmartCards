package com.example.smart_cards

import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_cards.models.UserModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainMenuActivity: AppCompatActivity() {
    private lateinit var navigation: BottomNavigationView
    private val userViewModel: UserModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu_layout)
        supportFragmentManager.beginTransaction().replace(R.id.main_menu_fragment, HomeFragment.instance).commit()
        findObjects()
        setNavigationListeners()
    }

    private fun findObjects(){
        navigation=findViewById<BottomNavigationView>(R.id.bottom_navigation)
    }

    private fun setNavigationListeners() {
        navigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_menu_fragment, HomeFragment.instance)
                        .commit()
                    true
                }

                R.id.navigation_learn -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_menu_fragment, LevelFragment())
                        .commit()
                    true
                }

                R.id.navigation_profile -> {
                   supportFragmentManager.beginTransaction()
                       .replace(R.id.main_menu_fragment, ProfileFragment.instance)
                       .commit()
                    true
                }

                else -> false
            }

        }
    }
}