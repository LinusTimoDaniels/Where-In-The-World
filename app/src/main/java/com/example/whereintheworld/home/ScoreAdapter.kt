package com.example.whereintheworld.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whereintheworld.data.ScoreEntry
import com.example.whereintheworld.databinding.ItemScoreBinding

import java.text.SimpleDateFormat
import java.util.*

class ScoreAdapter(private val scoreList: List<ScoreEntry>) : RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    inner class ScoreViewHolder(private val binding: ItemScoreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(scoreEntry: ScoreEntry) {

            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(scoreEntry.timestamp))

            val formattedDistance = String.format("%.3f", scoreEntry.distance)

            binding.scoreTextView.text = "Score: ${scoreEntry.score}, Distance: ${formattedDistance} km"
            binding.timestampTextView.text = "Date: $formattedDate"
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

