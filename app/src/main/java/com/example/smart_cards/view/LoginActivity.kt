package com.example.smart_cards.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_cards.R
import com.example.smart_cards.db.dbAdapter
import com.example.smart_cards.repository.UsersRepository
import com.example.smart_cards.viewModels.UserViewModel
import com.google.android.material.textfield.TextInputEditText



class LoginActivity: AppCompatActivity() {
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var viewModel: UserViewModel

    private var login: Boolean=false


    private lateinit var repository: UsersRepository

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)
        dbAdapter.init(applicationContext)
        repository = UsersRepository(dbAdapter)
        viewModel= UserViewModel(repository)
        initButtons()
        setListeners()


    }
    private fun initButtons(){
        loginButton=findViewById<Button>(R.id.login_button)
        registerButton=findViewById<Button>(R.id.register_button)
        emailInput=findViewById<TextInputEditText>(R.id.email_input)
        passwordInput=findViewById<TextInputEditText>(R.id.password_input)
    }

    private fun setListeners(){
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            viewModel.login(email, password)
            startMainActivity()
        }
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }
    }
    private fun startMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}