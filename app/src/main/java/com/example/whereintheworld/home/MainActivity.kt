package com.example.whereintheworld.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whereintheworld.R
import com.example.whereintheworld.data.ScoreStorage
import com.example.whereintheworld.databinding.ActivityMainBinding
import com.example.whereintheworld.game.GameActivity
import com.example.whereintheworld.notification.NotificationService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var scoreAdapter: ScoreAdapter

    // Register the permission request result callback
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // No Toasts for permission status
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

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

            val totalPoints = scores.sumOf { it.score } // Calculate total points
            binding.totalPointsTextView.text = getString(R.string.total_points, totalPoints)
        }

        scoreAdapter = ScoreAdapter(scores)
        binding.scoresRecyclerView.adapter = scoreAdapter

        binding.startGameButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        // Request notification permission if targeting Android 13 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if the user has granted notification permission
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Request notification permission
                requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Start the notification service
        val serviceIntent = Intent(this, NotificationService::class.java)
        startService(serviceIntent)
    }
}
