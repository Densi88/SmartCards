package com.example.smart_cards

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_cards.db.dbAdapter
import com.google.android.material.textfield.TextInputEditText

import java.security.MessageDigest

class RegisterActivity: AppCompatActivity() {
    private lateinit var registerButton: Button
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var usernameInputEditText: TextInputEditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    private fun register(hash_password:String, email:String, username:String){
        val dbHelper= dbAdapter.getDbHelper()
        val db=dbHelper.writableDatabase
        val query="insert into user(hash_password, login, username) values (?, ?, ?)"
        db.execSQL(query, arrayOf(hash_password, email, username))
        Toast.makeText(this, "Пользователь зарегистрирован", Toast.LENGTH_SHORT).show()

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
            val hash=simpleHash(password)
            register(hash, email, username)
            startLogin()
        }

    }

    private fun simpleHash(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}