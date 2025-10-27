package com.example.smart_cards

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    private lateinit var enterButton: Button
    private lateinit var exitButton: Button
    private lateinit var settingsButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        findObjects()
        setListeners()
    }
    private fun setListeners(){
        enterButton.setOnClickListener {
            val intent= Intent(this, MainMenuActivity::class.java)
            startActivity(intent)

        }
        exitButton.setOnClickListener {
            finishAffinity()
        }
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
    private fun findObjects(){
        enterButton=findViewById<Button>(R.id.enterButton)
        exitButton=findViewById<Button>(R.id.exitButton)
        settingsButton=findViewById<Button>(R.id.settingsButton)
    }

}