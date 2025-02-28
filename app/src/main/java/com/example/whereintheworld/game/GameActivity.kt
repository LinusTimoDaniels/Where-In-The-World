package com.example.whereintheworld.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whereintheworld.R
import com.example.whereintheworld.data.ScoreSavingService
import com.example.whereintheworld.databinding.ActivityGameBinding
import com.example.whereintheworld.home.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GameActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGameBinding
    private val gameViewModel: GameViewModel by viewModels()
    private var myMap: GoogleMap? = null

    private var userMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleDeepLink()
        setupEdgeToEdge()
        startGame()
        observeViewModel()
    }

    private fun setupEdgeToEdge() {
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.game) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun handleDeepLink() {
        val intent = intent
        val action = intent.action
        val data = intent.data

        if (Intent.ACTION_VIEW == action && data != null) {
            val lat = data.getQueryParameter("lat")?.toDoubleOrNull()
            val lng = data.getQueryParameter("lng")?.toDoubleOrNull()

            if (lat != null && lng != null) {
                val location = LatLng(lat, lng)
                gameViewModel.setLocation(location)
            } else {
                Toast.makeText(this, "Invalid coordinates in the link", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        gameViewModel.location.observe(this) { newLocation ->
            setupStreetView(newLocation)
        }

        gameViewModel.userGuess.observe(this) { guess ->
            updateGuessMarker(guess)
            toggleEndGameButtonVisibility(guess)
        }

        gameViewModel.isMapExpanded.observe(this) { isExpanded ->
            adjustMapSize(isExpanded)
        }
    }

    private fun startGame() {
        setupMap()
        if (gameViewModel.location.value == null) {
            gameViewModel.generateLocation()
        }
        setupUIButtons()
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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

    private fun setupUIButtons() {
        val toggleButton = findViewById<FloatingActionButton>(R.id.toggleMapSizeButton)
        val endGameButton = findViewById<FloatingActionButton>(R.id.endGameButton)
        val shareButton = findViewById<Button>(R.id.shareMapButton)

        toggleButton.setOnClickListener { gameViewModel.toggleMapSize() }
        endGameButton.setOnClickListener { endGame() }
        shareButton.setOnClickListener { shareMapLocation() }

        endGameButton.visibility = View.GONE
    }

    private fun toggleEndGameButtonVisibility(guess: LatLng?) {
        val endGameButton = findViewById<FloatingActionButton>(R.id.endGameButton)
        endGameButton.visibility = if (guess != null) View.VISIBLE else View.GONE
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
        val serviceIntent = Intent(this, ScoreSavingService::class.java).apply {
            putExtra("distance", gameViewModel.distance.value)
            putExtra("score", gameViewModel.score.value)
        }
        startService(serviceIntent)

        updateUIForEndGame()
        myMap?.setOnMapClickListener(null)
        addGameMarkersAndPolyline()
    }

    private fun updateUIForEndGame() {
        findViewById<View>(R.id.streetView).visibility = View.GONE
        findViewById<View>(R.id.gameEndLayout).visibility = View.VISIBLE
        findViewById<View>(R.id.endGameButton).visibility = View.GONE
        val gameEndMessageTextView: TextView = findViewById(R.id.gameEndMessage)

        gameEndMessageTextView.text = "Distance: ${gameViewModel.distance.value} km \nYour Score: ${gameViewModel.score.value}"

        val backToHomeButton = findViewById<Button>(R.id.backToHomeButton)
        backToHomeButton.visibility = View.VISIBLE

        backToHomeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun addGameMarkersAndPolyline() {
        val location = gameViewModel.location.value!!
        val userGuess = gameViewModel.userGuess.value!!

        myMap?.addMarker(
            MarkerOptions()
                .position(location)
                .title("Actual Location")
                .snippet("Actual City")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        myMap?.addPolyline(
            PolylineOptions()
                .add(userGuess, location)
                .width(10f)
                .color(getColor(R.color.black))
                .pattern(listOf(Dash(20f), Gap(10f)))
        )

        myMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 7.5f))
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
        myMap?.setOnMapClickListener { latLng -> gameViewModel.setUserGuess(latLng) }

        gameViewModel.userGuess.value?.let { guess -> updateGuessMarker(guess) }
    }

    private fun shareMapLocation() {
        val location = gameViewModel.location.value!!
        val mapUri = "https://dynamiclinksfa.azurewebsites.net/startgame?lat=${location.latitude}&lng=${location.longitude}"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this location on the map: $mapUri")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Share Map"))
        } catch (e: Exception) {
            Toast.makeText(this, "No app available to share", Toast.LENGTH_SHORT).show()
        }
    }
}
