package com.example.game

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object AppStorage {

    private const val PREFS = "memory_game_data"
    private const val GAME = "saved_game"
    private const val RECORDS = "records"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun hasGame(context: Context): Boolean =
        prefs(context).contains(GAME)

    fun saveGame(context: Context, data: JSONObject) {
        prefs(context).edit()
            .putString(GAME, data.toString())
            .apply()
    }

    fun loadGame(context: Context): JSONObject? {
        val raw = prefs(context).getString(GAME, null)
            ?: return null

        return try {
            JSONObject(raw)
        } catch (_: Exception) {
            null
        }
    }

    fun clearGame(context: Context) {
        prefs(context).edit().remove(GAME).apply()
    }

    fun saveRecord(
        context: Context,
        name: String,
        moves: Int,
        seconds: Int,
        size: Int
    ) {
        val records = loadRecords(context)

        val record = JSONObject().apply {
            put("name", name)
            put("moves", moves)
            put("seconds", seconds)
            put("size", size)
        }

        records.put(record)

        val list = (0 until records.length())
            .map { records.getJSONObject(it) }
            .sortedWith(
                compareBy<JSONObject> { it.optInt("moves") }
                    .thenBy { it.optInt("seconds") }
            )
            .take(20)

        val result = JSONArray()
        list.forEach { result.put(it) }

        prefs(context).edit()
            .putString(RECORDS, result.toString())
            .apply()
    }

    fun loadRecords(context: Context): JSONArray {
        val raw = prefs(context).getString(RECORDS, "[]")

        return try {
            JSONArray(raw)
        } catch (_: Exception) {
            JSONArray()
        }
    }
}
