package com.example.whereintheworld.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whereintheworld.R
import com.example.whereintheworld.data.ScoreStorage
import com.example.whereintheworld.databinding.ActivityMainBinding
import com.example.whereintheworld.game.GameActivity
import com.example.whereintheworld.notification.NotificationScheduler

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var scoreAdapter: ScoreAdapter

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                NotificationScheduler.scheduleDailyNotification(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.scoresRecyclerView.layoutManager = LinearLayoutManager(this)

        val scoreStorage = ScoreStorage(this)
        val scores = scoreStorage.getScores()

        if (scores.isEmpty()) {
            binding.scoresRecyclerView.visibility = android.view.View.GONE
            binding.noDataTextView.visibility = android.view.View.VISIBLE
            binding.totalPointsTextView.text = getString(R.string.total_points, 0)
        } else {
            binding.scoresRecyclerView.visibility = android.view.View.VISIBLE
            binding.noDataTextView.visibility = android.view.View.GONE
            val totalPoints = scores.sumOf { it.score }
            binding.totalPointsTextView.text = getString(R.string.total_points, totalPoints)
        }

        scoreAdapter = ScoreAdapter(scores)
        binding.scoresRecyclerView.adapter = scoreAdapter

        binding.startGameButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                NotificationScheduler.scheduleDailyNotification(this)
            }
        } else {
            NotificationScheduler.scheduleDailyNotification(this)
        }
    }
}
