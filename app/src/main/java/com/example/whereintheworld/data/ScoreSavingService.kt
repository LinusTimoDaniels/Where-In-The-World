    package com.example.whereintheworld.data

    import android.app.Service
    import android.content.Context
    import android.content.Intent
    import android.os.IBinder
    import android.util.Log
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch

    class ScoreSavingService : Service() {

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            val score = intent?.getIntExtra("score", 0) ?: 0
            val distance = intent?.getDoubleExtra("distance", 0.0)?.toFloat() ?: 0f

            Log.d("ScoreSavingService", "Received score: $score, distance: $distance")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val scoreStorage = ScoreStorage(applicationContext)
                    scoreStorage.saveScore(score, distance)
                    Log.d("ScoreSavingService", "Score saved successfully")
                } catch (e: Exception) {
                    Log.e("ScoreSavingService", "Error saving score: $e")
                } finally {
                    stopSelf()
                }
            }

            return START_NOT_STICKY
        }

        override fun onBind(intent: Intent?): IBinder? = null
    }

