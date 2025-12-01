package com.example.smart_cards.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class dbOpenHelper(context: Context): SQLiteOpenHelper(context, "Smart_cards_db", null, 1 ) {

    override fun onCreate(db: SQLiteDatabase){
        var createTable: String
        //Создание юзеров
        createTable="CREATE TABLE user (\n" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    hash_password TEXT NOT NULL,\n" +
                "    login TEXT UNIQUE NOT NULL,\n" +
                "    username TEXT NOT NULL\n" +
                ");"
        db.execSQL(createTable)
        //Создание языков
        createTable="CREATE TABLE language (\n" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    language_name TEXT NOT NULL UNIQUE\n" +
                ");"
        db.execSQL(createTable)
        //Создание переводов
        createTable="CREATE TABLE translation (\n" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    translation_text TEXT NOT NULL\n" +
                ");"
        db.execSQL(createTable)
        //Создание карточек
        createTable="CREATE TABLE cards (\n" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    word TEXT NOT NULL,\n" +
                "    translation_id INTEGER NOT NULL,\n" +
                "    language_id INTEGER NOT NULL,\n" +
                "    FOREIGN KEY (translation_id) REFERENCES translation(id) ON DELETE CASCADE,\n" +
                "    FOREIGN KEY (language_id) REFERENCES language(id) ON DELETE CASCADE\n" +
                ");"
        db.execSQL(createTable)
        //Создание уровней
        createTable="CREATE TABLE levels (\n" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    number INTEGER NOT NULL UNIQUE\n" +
                ");"
        db.execSQL(createTable)

        //Создание конкретных карточек для уровней
        createTable="CREATE TABLE current_level_cards (\n" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    level_id INTEGER NOT NULL,\n" +
                "    card_id INTEGER NOT NULL,\n" +
                "    FOREIGN KEY (level_id) REFERENCES levels(id),\n" +
                "    FOREIGN KEY (card_id) REFERENCES cards(id),\n" +
                "    UNIQUE(level_id, card_id)\n" +
                ");"
        db.execSQL(createTable)
        //Создание статистики
        createTable="CREATE TABLE user_statistics (\n" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    days_in_row INTEGER DEFAULT 0,\n" +
                "    level INTEGER DEFAULT 1,\n" +
                "    experience INTEGER DEFAULT 0,\n" +
                "    user_id INTEGER NOT NULL,\n" +
                "    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE\n" +
                ");"
        db.execSQL(createTable)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int){
        db.execSQL("DROP TABLE IF EXISTS user_statistics")
        db.execSQL("DROP TABLE IF EXISTS current_level_cards")
        db.execSQL("DROP TABLE IF EXISTS cards")
        db.execSQL("DROP TABLE IF EXISTS translation")
        db.execSQL("DROP TABLE IF EXISTS levels")
        db.execSQL("DROP TABLE IF EXISTS language")
        db.execSQL("DROP TABLE IF EXISTS user")
        onCreate(db)
    }
}