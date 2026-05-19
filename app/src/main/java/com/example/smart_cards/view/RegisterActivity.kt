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

class RegisterActivity: AppCompatActivity() {
    private lateinit var registerButton: Button
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var usernameInputEditText: TextInputEditText
    private lateinit var viewModel: UserViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbAdapter.init(applicationContext)
        val repository = UsersRepository(dbAdapter)
        viewModel = UserViewModel(repository)
        setContentView(R.layout.registration_layout)
        initView()
        setListeners()

    }
    private fun initView(){
        registerButton=findViewById<Button>(R.id.button_reg)
        passwordInput=findViewById<TextInputEditText>(R.id.password_input_reg)
        emailInput=findViewById<TextInputEditText>(R.id.email_input_reg)
        usernameInputEditText=findViewById<TextInputEditText>(R.id.name_input_reg)
    }
    private fun startLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
    private fun setListeners(){
        registerButton.setOnClickListener {
            var password=passwordInput.text.toString().trim()
            var email=emailInput.text.toString().trim()
            var username=usernameInputEditText.text.toString().trim()
            viewModel.register(email, username, password)
            startLogin()
        }

    }
}