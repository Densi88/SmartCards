package com.example.smart_cards

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.smart_cards.ui.theme.Smart_cardsTheme

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

        }
        exitButton.setOnClickListener {
            finishAffinity()
        }
        settingsButton.setOnClickListener {
            setContentView(R.layout.settings_layout)
        }
    }
    private fun findObjects(){
        enterButton=findViewById<Button>(R.id.enterButton)
        exitButton=findViewById<Button>(R.id.exitButton)
        settingsButton=findViewById<Button>(R.id.settingsButton)
    }

}