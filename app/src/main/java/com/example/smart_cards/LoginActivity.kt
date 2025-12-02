package com.example.smart_cards

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smart_cards.db.dbAdapter
import com.example.smart_cards.models.UserModel
import com.google.android.material.textfield.TextInputEditText
import java.security.MessageDigest


class LoginActivity: AppCompatActivity() {
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    private var login: Boolean=false

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)
        dbAdapter.init(applicationContext)

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
            login()

        }
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }
    }
    private fun login(){
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val hash=simpleHash(password)
        val user=authenticateUser(email, hash)
        if(user==null){
            Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show()
        }
        else{
            startMainActivity()
        }
    }

    private fun authenticateUser(email: String, hash_password: String): UserModel? {
        val dbHelper = dbAdapter.getDbHelper()
        val db = dbHelper.readableDatabase

        // Пример SQL запроса
        val query = """
            SELECT login, username 
            FROM user 
            WHERE login = ? AND hash_password = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(email, hash_password))

        return if (cursor.moveToFirst()) {
            val userEmail = cursor.getString(cursor.getColumnIndexOrThrow("login"))
            val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))

            cursor.close()
            UserModel(userEmail, username)
        } else {
            cursor.close()
            null
        }
    }

    private fun simpleHash(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun startMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}