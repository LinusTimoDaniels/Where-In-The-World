package com.example.whereintheworld.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

class GameViewModel : ViewModel() {

    private val _location = MutableLiveData<LatLng>()
    val location: LiveData<LatLng> get() = _location

    private val _userGuess = MutableLiveData<LatLng>()
    val userGuess: LiveData<LatLng> get() = _userGuess

    private val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double> get() = _distance

    private val _isMapExpanded = MutableLiveData(false)
    val isMapExpanded: LiveData<Boolean> get() = _isMapExpanded

    private val _socre = MutableLiveData<Int>()
    val score: LiveData<Int> get() = _socre


    fun generateLocation() {
        val latitude = 48.859318
        val longitude = 2.292737
        _location.value = LatLng(latitude, longitude)
    }

    fun setUserGuess(latLng: LatLng) {
        _userGuess.value = latLng
        calculateDistance()
    }

    fun calculateDistance() {
        val guess = _userGuess.value
        val actual = _location.value
        if (guess != null && actual != null) {
            _distance.value = SphericalUtil.computeDistanceBetween(guess, actual) / 1000
            calculateScore()
        }
    }

    fun calculateScore() {
        val distance = _distance.value ?: return  // Get the computed distance

        val maxScore = 5000  // Maximum score
        val maxDistance = 500000  // Maximum distance (e.g., 500 km, or 500,000 meters)

        // If the distance is less than the max distance, calculate the score
        val score = if (distance < maxDistance) {
            val score = maxScore * (1 - (distance / maxDistance.toDouble()))
            score.toInt()  // Convert to integer
        } else {
            0  // If the distance is larger than the max distance, score is 0
        }

        _socre.value = score
    }


    fun toggleMapSize() {
        _isMapExpanded.value = _isMapExpanded.value != true
    }
}
