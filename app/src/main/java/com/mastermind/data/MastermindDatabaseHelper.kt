package com.mastermind.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MastermindDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "mastermind.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS saved_games (
                id INTEGER PRIMARY KEY,
                json TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS current_game (
                id INTEGER PRIMARY KEY,
                json TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS game_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                json TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS saved_games")
            db.execSQL("DROP TABLE IF EXISTS current_game")
            db.execSQL("DROP TABLE IF EXISTS game_history")
            onCreate(db)
        }
    }
}