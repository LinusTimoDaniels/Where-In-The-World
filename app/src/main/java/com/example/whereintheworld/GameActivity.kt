package com.example.whereintheworld

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.whereintheworld.databinding.ActivityGameBinding
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.SphericalUtil

class GameActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGameBinding
    private lateinit var myMap: GoogleMap
    private var userMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.game) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap

        // Set up the map properties
        myMap.uiSettings.isZoomControlsEnabled = true

        // Define a location (for example, Sydney, Australia)
        val sydney = LatLng(-33.8688, 151.2093)

        // Move camera to the location (Sydney)
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10f))

        // Add a marker for Sydney
        myMap.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Sydney")
                .snippet("This is Sydney, Australia")
        )

        // Handle user taps to place a marker
        myMap.setOnMapClickListener { latLng ->
            // Remove the previous marker if exists
            userMarker?.remove()

            // Add a new marker where the user taps
            userMarker = myMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("User Marker")
            )

            // Calculate the distance from the user's marker to Sydney
            val distance = SphericalUtil.computeDistanceBetween(latLng, sydney)

            // Show the distance as a Toast
            Toast.makeText(this, "Distance to Sydney: ${distance / 1000} km", Toast.LENGTH_LONG).show()
        }
    }
}