package com.example.whereintheworld.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScoreStorage(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("game_scores", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveScore(score: Int, distance: Float) {
        val scoresList = getScores().toMutableList()
        val newEntry = ScoreEntry(score, distance, System.currentTimeMillis())

        scoresList.add(newEntry)
        val jsonString = gson.toJson(scoresList)
        sharedPreferences.edit().putString("scores", jsonString).apply()
    }

    fun getScores(): List<ScoreEntry> {
        val jsonString = sharedPreferences.getString("scores", null) ?: return emptyList()
        val type = object : TypeToken<List<ScoreEntry>>() {}.type
        return gson.fromJson(jsonString, type) ?: emptyList()
    }

    fun clearScores() {
        sharedPreferences.edit().remove("scores").apply()
    }
}
