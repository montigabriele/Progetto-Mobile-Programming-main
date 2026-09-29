package com.mastermind.data

import android.content.ContentValues
import android.content.Context
import org.json.JSONObject
import org.json.JSONArray
import com.mastermind.models.*
import java.util.Date

class GameRepository(context: Context) {

    private val dbHelper = MastermindDatabaseHelper(context)

    fun saveGame(gameState: GameState) {
        val db = dbHelper.writableDatabase
        val json = gameStateToJson(gameState)
        val values = ContentValues().apply {
            put("id", gameState.id)
            put("json", json)
        }

        val cursor = db.query("saved_games", arrayOf("id"), "id = ?", arrayOf(gameState.id.toString()), null, null, null)
        val exists = cursor.moveToFirst()
        cursor.close()

        if (exists) {
            db.update("saved_games", values, "id = ?", arrayOf(gameState.id.toString()))
        } else {
            db.insert("saved_games", null, values)
        }

        db.close()
    }

    fun getSavedGames(): List<GameState> {
        val db = dbHelper.readableDatabase
        val cursor = db.query("saved_games", arrayOf("json"), null, null, null, null, "id DESC")
        val games = mutableListOf<GameState>()
        while (cursor.moveToNext()) {
            val json = cursor.getString(0)
            try {
                val gameState = jsonToGameState(json)
                games.add(gameState)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        cursor.close()
        db.close()
        return games
    }

    fun deleteSavedGame(gameId: Int) {
        val db = dbHelper.writableDatabase

        db.delete("saved_games", "id = ?", arrayOf(gameId.toString()))
        db.delete("current_game", "id = ?", arrayOf(gameId.toString()))

        val cursor = db.query(
            "game_history",
            arrayOf("id", "json"),
            null,
            null,
            null,
            null,
            null
        )

        val historyRowsToDelete = mutableListOf<Int>()

        while (cursor.moveToNext()) {
            val historyRowId = cursor.getInt(0)
            val json = cursor.getString(1)

            try {
                val historyJson = JSONObject(json)

                if (historyJson.optInt("id", -1) == gameId) {
                    historyRowsToDelete.add(historyRowId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        cursor.close()

        historyRowsToDelete.forEach { historyRowId ->
            db.delete(
                "game_history",
                "id = ?",
                arrayOf(historyRowId.toString())
            )
        }

        db.close()
    }

    fun saveCurrentGame(gameState: GameState) {
        val db = dbHelper.writableDatabase
        val json = gameStateToJson(gameState)
        val values = ContentValues().apply {
            put("id", gameState.id)
            put("json", json)
        }

        db.delete("current_game", null, null)
        db.insert("current_game", null, values)

        db.close()

        saveGame(gameState)
    }

    fun getCurrentGame(): GameState? {
        val db = dbHelper.readableDatabase
        val cursor = db.query("current_game", arrayOf("json"), null, null, null, null, null)
        val game = if (cursor.moveToFirst()) {
            try {
                jsonToGameState(cursor.getString(0))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else null
        cursor.close()
        db.close()
        return game
    }

    fun clearCurrentGame(gameId: Int) {
        val db = dbHelper.writableDatabase
        db.delete(
            "current_game",
            "id = ?",
            arrayOf(gameId.toString())
        )
        db.close()
    }

    fun hasCurrentGame(): Boolean {
        val currentGame = getCurrentGame()
        return currentGame != null && !currentGame.isGameOver
    }

    fun addHistory(history: GameHistory) {
        val db = dbHelper.writableDatabase
        val json = gameHistoryToJson(history)
        val values = ContentValues().apply {
            put("json", json)
        }
        db.insert("game_history", null, values)
        db.close()
    }


    private fun gameStateToJson(gameState: GameState): String {
        val json = JSONObject()
        json.put("id", gameState.id)
        json.put("settings", gameSettingsToJson(gameState.settings))
        json.put("secretCode", colorPegListToJsonArray(gameState.secretCode))
        json.put("attempts", attemptsToJsonArray(gameState.attempts))
        json.put("isWon", gameState.isWon)
        json.put("isGameOver", gameState.isGameOver)
        json.put("compatibleMovesCount", gameState.compatibleMovesCount)
        json.put("date", gameState.date.time)
        json.put("gameTime", gameState.gameTime)
        return json.toString()
    }

    private fun jsonToGameState(jsonString: String): GameState {
        val json = JSONObject(jsonString)
        return GameState(
            id = json.getInt("id"),
            settings = jsonToGameSettings(json.getJSONObject("settings")),
            secretCode = jsonArrayToColorPegList(json.getJSONArray("secretCode")),
            attempts = jsonArrayToAttempts(json.getJSONArray("attempts")),
            isWon = json.getBoolean("isWon"),
            isGameOver = json.getBoolean("isGameOver"),
            compatibleMovesCount = json.getLong("compatibleMovesCount"),
            date = Date(json.getLong("date")),
            gameTime = json.optInt("gameTime", 0)
        )
    }

    private fun gameSettingsToJson(settings: GameSettings): JSONObject {
        val json = JSONObject()
        json.put("numColors", settings.numColors)
        json.put("codeLength", settings.codeLength)
        json.put("allowDuplicates", settings.allowDuplicates)
        json.put("maxAttempts", settings.maxAttempts)
        return json
    }

    private fun jsonToGameSettings(json: JSONObject): GameSettings {
        return GameSettings(
            numColors = json.getInt("numColors"),
            codeLength = json.getInt("codeLength"),
            allowDuplicates = json.getBoolean("allowDuplicates"),
            maxAttempts = json.getInt("maxAttempts")
        )
    }

    private fun colorPegListToJsonArray(colorPegs: List<ColorPeg>): JSONArray {
        val jsonArray = JSONArray()
        colorPegs.forEach { colorPeg ->
            val colorJson = JSONObject()
            colorJson.put("name", colorPeg.name)
            colorJson.put("color", colorPeg.color)
            jsonArray.put(colorJson)
        }
        return jsonArray
    }

    private fun jsonArrayToColorPegList(jsonArray: JSONArray): List<ColorPeg> {
        val colorPegs = mutableListOf<ColorPeg>()
        for (i in 0 until jsonArray.length()) {
            val colorJson = jsonArray.getJSONObject(i)
            val name = colorJson.getString("name")
            val peg = ColorPeg.valueOf(name)
            colorPegs.add(peg)
        }
        return colorPegs
    }

    private fun attemptsToJsonArray(attempts: List<Attempt>): JSONArray {
        val jsonArray = JSONArray()
        attempts.forEach { attempt ->
            val attemptJson = JSONObject()
            attemptJson.put("guess", colorPegListToJsonArray(attempt.guess))
            attemptJson.put("correctPosition", attempt.correctPosition)
            attemptJson.put("correctColor", attempt.correctColor)
            jsonArray.put(attemptJson)
        }
        return jsonArray
    }

    private fun jsonArrayToAttempts(jsonArray: JSONArray): List<Attempt> {
        val attempts = mutableListOf<Attempt>()
        for (i in 0 until jsonArray.length()) {
            val attemptJson = jsonArray.getJSONObject(i)
            val attempt = Attempt(
                guess = jsonArrayToColorPegList(attemptJson.getJSONArray("guess")),
                correctPosition = attemptJson.getInt("correctPosition"),
                correctColor = attemptJson.getInt("correctColor")
            )
            attempts.add(attempt)
        }
        return attempts
    }

    private fun gameHistoryToJson(history: GameHistory): String {
        val json = JSONObject()
        json.put("id", history.id)
        json.put("settings", gameSettingsToJson(history.settings))
        json.put("isWon", history.isWon)
        json.put("score", history.score)
        json.put("attempts", history.attempts)
        json.put("date", history.date.time)
        return json.toString()
    }
}