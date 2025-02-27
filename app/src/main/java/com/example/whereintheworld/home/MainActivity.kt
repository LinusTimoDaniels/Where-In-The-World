package com.example.whereintheworld.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

        binding.scoresRecyclerView.layoutManager = LinearLayoutManager(this)

        val scoreStorage = ScoreStorage(this)
        val scores = scoreStorage.getScores()

        if (scores.isEmpty()) {
            binding.scoresRecyclerView.visibility = android.view.View.GONE
            binding.noDataTextView.visibility = android.view.View.VISIBLE
        } else {
            binding.scoresRecyclerView.visibility = android.view.View.VISIBLE
            binding.noDataTextView.visibility = android.view.View.GONE
        }

        scoreAdapter = ScoreAdapter(scores)
        binding.scoresRecyclerView.adapter = scoreAdapter

        binding.startGameButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }
}
