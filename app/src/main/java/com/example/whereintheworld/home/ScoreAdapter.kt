package com.example.whereintheworld.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whereintheworld.data.ScoreEntry
import com.example.whereintheworld.databinding.ItemScoreBinding

class ScoreAdapter(private val scoreList: List<ScoreEntry>) : RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    // ViewHolder class to represent each item
    inner class ScoreViewHolder(private val binding: ItemScoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(scoreEntry: ScoreEntry) {
            binding.scoreTextView.text = "Score: ${scoreEntry.score}, Distance: ${scoreEntry.distance} km"
            binding.timestampTextView.text = "Timestamp: ${scoreEntry.timestamp}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val binding = ItemScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        holder.bind(scoreList[position])
    }

    override fun getItemCount(): Int {
        return scoreList.size
    }
}
