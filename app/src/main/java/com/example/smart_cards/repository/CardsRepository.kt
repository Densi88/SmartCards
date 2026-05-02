package com.example.smart_cards.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import com.example.smart_cards.models.Card
import java.sql.SQLException

class CardsRepository {
    private fun readCards(db: SQLiteDatabase){
        val query = "SELECT cards.word, translation.translation_text " +
                "FROM cards JOIN translation ON translation.id = cards.translation_id"
        val cursor=db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val card = Card(
                word = cursor.getString(cursor.getColumnIndexOrThrow("word")),
                translate = cursor.getString(cursor.getColumnIndexOrThrow("translation_text")),
            )
        }
        cursor.close()

    }

    private fun updateCard(db: SQLiteDatabase, newWord:String, newTranslation: String, oldWord:String){
        val cursor = db.rawQuery(
            "SELECT c.id, c.translation_id FROM cards c WHERE c.word = ?",
            arrayOf(oldWord)
        )
        if (!cursor.moveToFirst()) {
            cursor.close()
            return
        }
        val cardId = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
        val translationId = cursor.getLong(cursor.getColumnIndexOrThrow("translation_id"))
        cursor.close()

        val translationValues = ContentValues().apply {
            put("translation_text", newTranslation)
        }

        db.update(
            "translation",
            translationValues,
            "id = ?",
            arrayOf(translationId.toString())
        )

        // 3. Обновляем слово в карточке
        val cardValues = ContentValues().apply {
            put("word", newWord)
        }

        val rowsUpdated = db.update(
            "cards",
            cardValues,
            "id = ?",
            arrayOf(cardId.toString())
        )
    }

    private fun deleteCard(db: SQLiteDatabase, currentWord: String){
        val rowsDeleted = db.delete("cards", "word = ?", arrayOf(currentWord))
        if (rowsDeleted > 0) {
            Log.d("DB", "Удалена карточка: $currentWord (каскадно)")
        }
    }

    private fun addCard(currentWord: String, currentTranslation:String, db: SQLiteDatabase){
        val addTranslation="insert into translation (translation_text) values (?)"
        db.execSQL(addTranslation, arrayOf(currentTranslation))

        val cursor = db.rawQuery("SELECT last_insert_rowid()", null)
        val translationId = if (cursor.moveToFirst()) {
            cursor.getLong(0)
        } else {
            -1L
        }
        cursor.close()

        if (translationId == -1L) {
            throw SQLException("Не удалось получить ID перевода") as Throwable
        }


        val addCard = """
            INSERT INTO cards (word, translation_id, language_id) 
            VALUES(?, ?, ?)
        """.trimIndent()
        db.execSQL(addCard, arrayOf(currentWord, translationId.toString(), "1"))
    }


}