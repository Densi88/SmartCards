package com.example.smart_cards.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.smart_cards.db.dbAdapter
import com.example.smart_cards.models.Card
import java.sql.SQLException


class CardsRepository(
    private val dbAdapter: dbAdapter
) {

    private val dbHelper by lazy { dbAdapter.getDbHelper() }

    fun readCards(): List<Card> {
        val db = dbHelper.readableDatabase
        val query = "SELECT cards.word, translation.translation_text " +
                "FROM cards JOIN translation ON translation.id = cards.translation_id"
        val cursor = db.rawQuery(query, null)

        val cardsList = mutableListOf<Card>()

        while (cursor.moveToNext()) {
            val card = Card(
                word = cursor.getString(cursor.getColumnIndexOrThrow("word")),
                translate = cursor.getString(cursor.getColumnIndexOrThrow("translation_text"))
            )
            cardsList.add(card)
        }
        cursor.close()

        return cardsList
    }

     fun updateCard(newWord:String, newTranslation: String, oldWord:String){
        val db = dbHelper.writableDatabase
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

     fun deleteCard(currentWord: String){
        val db = dbHelper.writableDatabase
        val rowsDeleted = db.delete("cards", "word = ?", arrayOf(currentWord))
        if (rowsDeleted > 0) {
            Log.d("DB", "Удалена карточка: $currentWord (каскадно)")
        }
    }

     fun addCard(currentWord: String, currentTranslation:String){
        val db = dbHelper.writableDatabase
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

    fun addCardToLevel(cardWord: String, levelNumber: Int) {
        val db = dbHelper.writableDatabase

        // Получаем ID карточки
        val cardCursor = db.rawQuery("SELECT id FROM cards WHERE word = ?", arrayOf(cardWord))
        if (!cardCursor.moveToFirst()) {
            cardCursor.close()
            throw Exception("Карточка не найдена")
        }
        val cardId = cardCursor.getLong(cardCursor.getColumnIndexOrThrow("id"))
        cardCursor.close()

        // Получаем ID уровня
        val levelCursor = db.rawQuery("SELECT id FROM levels WHERE number = ?", arrayOf(levelNumber.toString()))
        if (!levelCursor.moveToFirst()) {
            levelCursor.close()
            throw Exception("Уровень не найден")
        }
        val levelId = levelCursor.getLong(levelCursor.getColumnIndexOrThrow("id"))
        levelCursor.close()

        // Добавляем связь
        val values = ContentValues().apply {
            put("level_id", levelId)
            put("card_id", cardId)
        }
        db.insert("current_level_cards", null, values)


    }



}