package com.example.smart_cards.repository;

import com.example.smart_cards.db.dbAdapter;
import com.example.smart_cards.models.UserModel;
import java.security.MessageDigest

public class UsersRepository {

    fun simpleHash(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
    fun register(hash_password:String, email:String, username:String){
        val dbHelper= dbAdapter.getDbHelper()
        val db=dbHelper.writableDatabase
        val query="insert into user(hash_password, login, username) values (?, ?, ?)"
        db.execSQL(query, arrayOf(hash_password, email, username))
    }

    fun authenticateUser(email: String, hash_password: String): UserModel? {
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

}
