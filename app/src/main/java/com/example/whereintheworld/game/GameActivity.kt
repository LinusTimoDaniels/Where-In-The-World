package com.example.whereintheworld.game

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

class GameActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGameBinding
    private val gameViewModel: GameViewModel by viewModels()
    private var myMap: GoogleMap? = null // Changed to nullable type

    private var userMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.game) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        startGame()
        observeViewModel()
    }

    private fun observeViewModel() {
        gameViewModel.location.observe(this) { newLocation ->
            setupStreetView(newLocation)
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
            panorama.setPosition(location)
            panorama.isStreetNamesEnabled = false
            panorama.isUserNavigationEnabled = true
            panorama.isPanningGesturesEnabled = true
            panorama.isZoomGesturesEnabled = true
            panorama.setOnStreetViewPanoramaChangeListener { streetViewPanoramaLocation ->
                if (streetViewPanoramaLocation?.links == null) {
                    Toast.makeText(this, "No Street View Available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun updateGuessMarker(guess: LatLng) {

        if (myMap != null && (userMarker == null || userMarker?.position != guess)) {
            userMarker?.remove()
            userMarker = myMap?.addMarker(
                MarkerOptions()
                    .position(guess)
                    .title("Your guess")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
        }
    }

    private fun endGame() {

        findViewById<View>(R.id.streetView).visibility = View.GONE
        findViewById<View>(R.id.gameEndLayout).visibility = View.VISIBLE

        findViewById<Button>(R.id.restartGameButton).setOnClickListener {

        }

        myMap?.addMarker(
            MarkerOptions()
                .position(gameViewModel.location.value!!)
                .title("Actual Location")
                .snippet("Actual City")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        myMap?.addPolyline(
            PolylineOptions()
                .add(gameViewModel.userGuess.value!!, gameViewModel.location.value!!)
                .width(10f)
                .color(getColor(R.color.black))
                .pattern(listOf(Dash(20f), Gap(10f)))
        )

        myMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(gameViewModel.location.value!!, 7.5f))
    }

    private fun adjustMapSize(isExpanded: Boolean) {
        val mapContainer = findViewById<View>(R.id.mapContainer)
        val params = mapContainer.layoutParams as ConstraintLayout.LayoutParams

        val displayMetrics = resources.displayMetrics
        val marginStartEnd = dpToPx(16)

        if (isExpanded) {
            val newSize = displayMetrics.widthPixels - (marginStartEnd * 2)
            params.width = newSize
            params.height = newSize
        } else {
            val collapsedSize = dpToPx(175)
            params.width = collapsedSize
            params.height = collapsedSize
        }

        mapContainer.layoutParams = params
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap

        myMap?.uiSettings?.apply {
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isRotateGesturesEnabled = true
            isTiltGesturesEnabled = true
            isZoomControlsEnabled = false
        }

        myMap?.moveCamera(CameraUpdateFactory.zoomOut())

        myMap?.setOnMapClickListener { latLng ->
            gameViewModel.setUserGuess(latLng)
        }

        gameViewModel.userGuess.value?.let { guess ->
            updateGuessMarker(guess)
        }
    }
}

