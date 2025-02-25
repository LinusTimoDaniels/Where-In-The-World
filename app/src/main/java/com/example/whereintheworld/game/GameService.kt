package com.example.whereintheworld.game

import android.app.Service
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.util.Locale
import kotlin.random.Random

class GameService : Service() {

    private val binder = LocalBinder()

    fun generateLocation(): LatLng {
        val latitude = 47.8584 //Random.nextDouble(-85.0, 85.0)
        val longitude = 2.2945 //Random.nextDouble(-180.0, 180.0)

        val newLocation = LatLng(latitude, longitude)

        return newLocation
    }

    fun calculateDistance(guess: LatLng, actual: LatLng): Double {
        return SphericalUtil.computeDistanceBetween(guess, actual)
    }

/**    fun getCityAndCountryFromCoordinates(lat: Double, lon: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocation(lat, lon, 1)
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val city = address.locality // City name
                val country = address.countryName // Country name

                Log.d("Location", "City: $city, Country: $country")
            } else {
                Log.d("Location", "No address found for the coordinates.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Location", "Geocoder failed to get the location details")
        }
    }
**/

    inner class LocalBinder : Binder() {
        fun getService(): GameService = this@GameService
    }

    override fun onBind(intent: Intent): IBinder {
        // Retrieve data from the intent (if provided)
        // ...

        return binder
    }
}
