package com.example.whereintheworld

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.whereintheworld.databinding.ActivityGameBinding
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.model.Marker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.SphericalUtil

class GameActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGameBinding
    private lateinit var myMap: GoogleMap
    private lateinit var streetViewPanorama: StreetViewPanorama
    private var userMarker: Marker? = null
    private var isMapExpanded = false

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

        // Initialize Street View
        val streetViewFragment = supportFragmentManager
            .findFragmentById(R.id.streetView) as SupportStreetViewPanoramaFragment
        streetViewFragment.getStreetViewPanoramaAsync { panorama ->
            streetViewPanorama = panorama

            // Set initial position
            streetViewPanorama.setPosition(LatLng(-33.8688, 151.2093)) // Example: Sydney

            // Enable controls for navigation
            streetViewPanorama.isStreetNamesEnabled = false // Enable street names display
            streetViewPanorama.isPanningGesturesEnabled = true // Enable panning (moving the view)
            streetViewPanorama.isUserNavigationEnabled = true // Enable user navigation
            streetViewPanorama.isZoomGesturesEnabled = true // Enable zoom gestures
        }

        // Initialize the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Toggle Button inside mapContainer
        val toggleButton = findViewById<FloatingActionButton>(R.id.toggleMapSizeButton)
        val mapContainer = findViewById<View>(R.id.mapContainer)

        toggleButton.setOnClickListener {
            toggleMapSize(mapContainer)
        }
    }

    private fun toggleMapSize(mapContainer: View) {
        val params = mapContainer.layoutParams

        if (isMapExpanded) {
            params.width = dpToPx(175) // Small size
            params.height = dpToPx(175)
        } else {
            params.width = dpToPx(400) // Large size
            params.height = dpToPx(400)
        }

        mapContainer.layoutParams = params
        isMapExpanded = !isMapExpanded
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap

        // Set up the map properties
        myMap.uiSettings.isZoomGesturesEnabled = true
        myMap.uiSettings.isScrollGesturesEnabled = true
        myMap.uiSettings.isRotateGesturesEnabled = true
        myMap.uiSettings.isTiltGesturesEnabled = true
        myMap.uiSettings.isZoomControlsEnabled = false

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

            // Update Street View position based on map tap
            streetViewPanorama.setPosition(latLng)

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

