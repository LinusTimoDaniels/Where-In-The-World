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
            _distance.value = SphericalUtil.computeDistanceBetween(guess, actual)
        }
    }

    fun toggleMapSize() {
        _isMapExpanded.value = _isMapExpanded.value != true
    }
}
