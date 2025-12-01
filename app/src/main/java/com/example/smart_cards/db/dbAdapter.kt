package com.example.smart_cards.db

import android.content.Context

object dbAdapter {
    private lateinit var dbHelper: dbOpenHelper

    fun init(context: Context) {
        dbHelper = dbOpenHelper(context.applicationContext)
    }

    fun getDbHelper(): dbOpenHelper = dbHelper
}
