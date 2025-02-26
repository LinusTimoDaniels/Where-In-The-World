package com.example.whereintheworld.game

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AppCompatActivity
import com.example.whereintheworld.R
import com.example.whereintheworld.databinding.ActivityGameBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.Marker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGameBinding
    private val gameViewModel: GameViewModel by viewModels()
    private lateinit var myMap: GoogleMap

    private var userMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup edge-to-edge
        enableEdgeToEdge()

        startGame()
        observeViewModel()
    }

    private fun observeViewModel() {
        gameViewModel.location.observe(this) { newLocation ->
            setupStreetView(newLocation)
            setupMap()
        }

        gameViewModel.userGuess.observe(this) { guess ->
            updateGuessMarker(guess)
        }

        gameViewModel.distance.observe(this) { distance ->
            Toast.makeText(this, "Distance to Location: ${distance / 1000} km", Toast.LENGTH_LONG).show()
        }

        gameViewModel.isMapExpanded.observe(this) { isExpanded ->
            adjustMapSize(isExpanded)
        }
    }

    private fun startGame() {
        setupMap()
        gameViewModel.generateLocation()

        // Map size toggle
        val toggleButton = findViewById<FloatingActionButton>(R.id.toggleMapSizeButton)
        val endGameButton = findViewById<FloatingActionButton>(R.id.endGameButton)

        toggleButton.setOnClickListener {
            gameViewModel.toggleMapSize()
        }

        endGameButton.setOnClickListener {
            endGame()
        }
    }

    private fun setupStreetView(location: LatLng) {
        val streetViewFragment = supportFragmentManager
            .findFragmentById(R.id.streetView) as SupportStreetViewPanoramaFragment
        streetViewFragment.getStreetViewPanoramaAsync { panorama ->
            lifecycleScope.launch(Dispatchers.Main) {
                panorama.setPosition(location)
                panorama.isStreetNamesEnabled = false
                panorama.isPanningGesturesEnabled = true
                panorama.isUserNavigationEnabled = true
                panorama.isZoomGesturesEnabled = true
            }
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun updateGuessMarker(guess: LatLng) {
        // Avoid adding/removing markers if the guess is unchanged
        if (userMarker == null || userMarker?.position != guess) {
            userMarker?.remove()
            userMarker = myMap.addMarker(
                MarkerOptions()
                    .position(guess)
                    .title("Your guess")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
        }
    }

    private fun endGame() {
        userMarker?.position?.let { gameViewModel.setUserGuess(it) }
        gameViewModel.calculateDistance()

        myMap.addMarker(
            MarkerOptions()
                .position(gameViewModel.location.value!!)
                .title("Actual Location")
                .snippet("Actual City")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        myMap.addPolyline(
            PolylineOptions()
                .add(gameViewModel.userGuess.value!!, gameViewModel.location.value!!)
                .width(10f)
                .color(getColor(R.color.black))
                .pattern(listOf(Dash(20f), Gap(10f)))
        )

        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gameViewModel.location.value!!, 7.5f))
    }

    private fun adjustMapSize(isExpanded: Boolean) {
        val mapContainer = findViewById<View>(R.id.mapContainer)
        val params = mapContainer.layoutParams

        // Dynamically adjust map size
        if (isExpanded) {
            params.width = dpToPx(400)
            params.height = dpToPx(400)
        } else {
            params.width = dpToPx(175)
            params.height = dpToPx(175)
        }

        mapContainer.layoutParams = params
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap

        // Map configuration
        myMap.uiSettings.isZoomGesturesEnabled = true
        myMap.uiSettings.isScrollGesturesEnabled = true
        myMap.uiSettings.isRotateGesturesEnabled = true
        myMap.uiSettings.isTiltGesturesEnabled = true
        myMap.uiSettings.isZoomControlsEnabled = false

        myMap.moveCamera(CameraUpdateFactory.zoomOut())

        myMap.setOnMapClickListener { latLng ->
            gameViewModel.setUserGuess(latLng)
        }
    }
}
