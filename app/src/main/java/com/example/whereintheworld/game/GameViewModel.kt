package com.example.whereintheworld.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import kotlin.random.Random

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

    private val locationList = listOf(
        LatLng(48.859318, 2.292737),    // Location 1 (Eiffel Tower, Paris)
        LatLng(40.748817, -73.985428),  // Location 2 (Empire State Building, New York)
        LatLng(51.5074, -0.1278),       // Location 3 (London)
        LatLng(34.0522, -118.2437),     // Location 4 (Los Angeles)
        LatLng(35.6895, 139.6917),      // Location 5 (Tokyo)
        LatLng(40.712776, -74.005974),  // Location 6 (New York City)
        LatLng(48.2082, 16.3738),       // Location 7 (Vienna)
        LatLng(37.7749, -122.4194),     // Location 8 (San Francisco)
        LatLng(52.5200, 13.4050),       // Location 9 (Berlin)
        LatLng(39.9042, 116.4074)       // Location 10 (Beijing)
    )


    fun generateLocation() {
        val randomLocation = locationList[Random.nextInt(locationList.size)]
        _location.value = randomLocation
    }

    fun setLocation(latLng: LatLng) {
        _location.value = latLng
    }

    fun setUserGuess(latLng: LatLng) {
        _userGuess.value = latLng
        calculateDistance()
    }

    fun calculateDistance() {
        val guess = _userGuess.value
        val actual = _location.value
        if (guess != null && actual != null) {
            _distance.value = String.format("%.3f", SphericalUtil.computeDistanceBetween(guess, actual) / 1000).toDouble()
            calculateScore()
        }
    }

    fun calculateScore() {
        val distance = _distance.value?.toInt() ?: return

        _socre.value = when {
            distance == 0 -> 5000
            distance >= 50000 -> 0
            else -> 5000 - (distance * 5000 / 50000)
        }
    }


    fun toggleMapSize() {
        _isMapExpanded.value = _isMapExpanded.value != true
    }
}
