package com.example.whereintheworld.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whereintheworld.data.ScoreStorage
import com.example.whereintheworld.databinding.ActivityMainBinding
import com.example.whereintheworld.game.GameActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var scoreAdapter: ScoreAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        // Set up RecyclerView
        binding.scoresRecyclerView.layoutManager = LinearLayoutManager(this)

        // Retrieve scores and set up the adapter
        val scoreStorage = ScoreStorage(this)
        val scores = scoreStorage.getScores()
        Log.d("MainActivity", "Loaded scores: $scores")

        // Check if there are scores
        if (scores.isEmpty()) {
            // If no scores, show the "No data available" message and hide the RecyclerView
            binding.scoresRecyclerView.visibility = android.view.View.GONE
            binding.noDataTextView.visibility = android.view.View.VISIBLE
        } else {
            // If there are scores, show the RecyclerView and hide the "No data available" message
            binding.scoresRecyclerView.visibility = android.view.View.VISIBLE
            binding.noDataTextView.visibility = android.view.View.GONE
        }

        // Set the adapter for the RecyclerView
        scoreAdapter = ScoreAdapter(scores)
        binding.scoresRecyclerView.adapter = scoreAdapter

        // Set up the start game button
        binding.startGameButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }
}
