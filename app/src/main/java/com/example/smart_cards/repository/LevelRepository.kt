package com.example.smart_cards.repository

import android.content.ContentValues
import com.example.smart_cards.db.dbAdapter
import com.example.smart_cards.models.Card

class LevelRepository(
    private val dbAdapter: dbAdapter
) {
    private val dbHelper by lazy { dbAdapter.getDbHelper() }

    fun getAvailableLevels(): List<Int> {
        val db = dbHelper.readableDatabase
        val query = "SELECT number FROM levels ORDER BY number"
        val cursor = db.rawQuery(query, null)

        val levels = mutableListOf<Int>()
        while (cursor.moveToNext()) {
            levels.add(cursor.getInt(cursor.getColumnIndexOrThrow("number")))
        }
        cursor.close()

        return levels
    }
    fun getCardsForLevel(levelNumber: Int): List<Card> {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT cards.word, translation.translation_text 
            FROM current_level_cards
            JOIN cards ON cards.id = current_level_cards.card_id
            JOIN translation ON translation.id = cards.translation_id
            JOIN levels ON levels.id = current_level_cards.level_id
            WHERE levels.number = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(levelNumber.toString()))

        val cards = mutableListOf<Card>()
        while (cursor.moveToNext()) {
            cards.add(
                Card(
                    word = cursor.getString(cursor.getColumnIndexOrThrow("word")),
                    translate = cursor.getString(cursor.getColumnIndexOrThrow("translation_text"))
                )
            )
        }
        cursor.close()
        return cards.shuffled()
    }
}