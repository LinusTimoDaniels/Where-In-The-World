package com.example.whereintheworld.game

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whereintheworld.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.whereintheworld.databinding.ActivityGameBinding
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.SphericalUtil

class GameActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGameBinding
    private lateinit var myMap: GoogleMap
    private lateinit var streetViewPanorama: StreetViewPanorama

    private lateinit var guess: LatLng
    private lateinit var location: LatLng
    private var distance: Double = 0.0

    private var gameService: GameService? = null
    private var isBound = false
    private var userMarker: Marker? = null
    private var isMapExpanded = false


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as GameService.LocalBinder
            gameService = binder.getService()
            isBound = true
            // After binding, calculate BMI
            startGame()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            gameService = null
            isBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        val intent = Intent(this, GameService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        ViewCompat.setOnApplyWindowInsetsListener(binding.game) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStop() {
        super.onStop()

        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    fun startGame() {
        if (isBound) {

            location = gameService?.generateLocation()!!

            val streetViewFragment = supportFragmentManager
                .findFragmentById(R.id.streetView) as SupportStreetViewPanoramaFragment
            streetViewFragment.getStreetViewPanoramaAsync { panorama ->
                streetViewPanorama = panorama

                streetViewPanorama.setPosition(location)

                streetViewPanorama.isStreetNamesEnabled = false
                streetViewPanorama.isPanningGesturesEnabled = true
                streetViewPanorama.isUserNavigationEnabled = true
                streetViewPanorama.isZoomGesturesEnabled = true
            }

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

            val toggleButton = findViewById<FloatingActionButton>(R.id.toggleMapSizeButton)
            val endGameButton = findViewById<FloatingActionButton>(R.id.endGameButton)
            val mapContainer = findViewById<View>(R.id.mapContainer)

            toggleButton.setOnClickListener {
                toggleMapSize(mapContainer)
            }

            endGameButton.setOnClickListener {
                endGame()
            }
        }
    }

    private fun endGame() {

        userMarker?.position?.let { guess = LatLng(it.latitude, it.longitude) }

        if (isBound)  {
            distance = gameService?.calculateDistance(guess, location)!!
        }

        val pattern: List<PatternItem> = listOf(Dash(20f), Gap(10f))

        myMap.addMarker(
            MarkerOptions()
            .position(location)
            .title("Actual country")
            .snippet("Actual city")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

        myMap.addPolyline(
            PolylineOptions()
                .add(guess, location)
                .width(10f)
                .color(getColor(R.color.black))
                .pattern(pattern)
        )

        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 7.5f))

        Toast.makeText(this, "Distance to Location: ${distance / 1000} km", Toast.LENGTH_LONG).show()
    }

    private fun toggleMapSize(mapContainer: View) {
        val params = mapContainer.layoutParams

        if (isMapExpanded) {
            params.width = dpToPx(175)
            params.height = dpToPx(175)
        } else {
            params.width = dpToPx(400)
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

        myMap.uiSettings.isZoomGesturesEnabled = true
        myMap.uiSettings.isScrollGesturesEnabled = true
        myMap.uiSettings.isRotateGesturesEnabled = true
        myMap.uiSettings.isTiltGesturesEnabled = true
        myMap.uiSettings.isZoomControlsEnabled = false

        myMap.moveCamera(CameraUpdateFactory.zoomOut())

        myMap.setOnMapClickListener { latLng ->
            userMarker?.remove()

            userMarker = myMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Your guess")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )

        }
    }
}

