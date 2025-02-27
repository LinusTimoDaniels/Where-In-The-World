package com.example.whereintheworld.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ScoreEntry(
    val score: Int,
    val distance: Float,
    val timestamp: Long
)
