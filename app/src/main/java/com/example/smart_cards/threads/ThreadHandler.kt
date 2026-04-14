package com.example.smart_cards.threads

import com.example.smart_cards.models.Card
import android.database.sqlite.SQLiteDatabase
import com.example.smart_cards.LearnFragment
import com.example.smart_cards.MainActivity
import com.example.smart_cards.db.dbAdapter


class ThreadHandler(private val onLevelsUpdate: (List<List<Card>>) -> Unit) {
    private val cardList = mutableListOf<Card>()
    @Volatile
    var running = true
    var firstLoadComplete = false
    private var lastWordCount = 0

    val loadWordsThread = Thread {
        val query = "SELECT cards.word, translation.translation_text " +
                "FROM cards JOIN translation ON translation.id = cards.translation_id"
        val dbHelper = dbAdapter.getDbHelper()
        val db = dbHelper.readableDatabase

        while (running) {
            if (Thread.currentThread().isInterrupted) {
                break
            }
            cardList.clear()
            println("Массив слов очищен")

            val cursor = db.rawQuery(query, null)
            while (cursor.moveToNext()) {
                val card = Card(
                    word = cursor.getString(cursor.getColumnIndexOrThrow("word")),
                    translate = cursor.getString(cursor.getColumnIndexOrThrow("translation_text")),
                )
                cardList.add(card)
            }
            cursor.close()
            println("Загружено ${cardList.size} слов")
            firstLoadComplete = true
            if (cardList.size != lastWordCount) {
                lastWordCount = cardList.size
            }
            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
                println("Поток загрузки прерван")
                break
            }
        }
        db.close()
    }
    val buildLevelsThread = Thread {
        while (!firstLoadComplete && running) {
            Thread.sleep(100)
        }
        var lastLevelsHash = 0
        while (running) {
            if (Thread.currentThread().isInterrupted) break
            val levels = cardList.chunked(10)
            val currentHash = levels.hashCode()
            if (currentHash != lastLevelsHash) {
                lastLevelsHash = currentHash
                println("Сформировано ${levels.size} уровней (НОВЫЕ)")
                onLevelsUpdate.invoke(levels)
            } else {
                println("Уровни не изменились, пропускаем")
            }
            try {
                Thread.sleep(10000)
            } catch (e: InterruptedException) {
                println("Поток генерации прерван")
                break
            }
        }
    }
    fun start() {
        loadWordsThread.start()
        buildLevelsThread.start()
    }

    fun stop() {
        running = false
        loadWordsThread.interrupt()
        buildLevelsThread.interrupt()
    }
}