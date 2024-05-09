package com.example.trainexplore

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.bumptech.glide.Glide
import com.example.trainexplore.database.AppDatabase
import com.example.trainexplore.entities.Favorito
import com.example.trainexplore.entities.Ponto_interesse
import com.example.trainexplore.loginSystem.SessionManager
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.StreetViewSource
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PoiMapActivity : AppCompatActivity(), OnMapReadyCallback, OnStreetViewPanoramaReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var estacaoLocation: LatLng
    private var lastClickedMarker: Marker? = null
    private lateinit var slidingPanel: SlidingUpPanelLayout
    private var mapInteractionJob: Job? = null
    private lateinit var mStreetViewPanorama: StreetViewPanorama

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi_map)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getApiKey())
        }
        placesClient = Places.createClient(this)
        setupMapFragment()
        setupMapAndStreetViewFragments()
        setupSlidingPanel()
        setupStreetViewToggle()
    }
    private fun getApiKey(): String {
        return packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData.getString("com.google.android.geo.API_KEY")
            ?: throw IllegalStateException("API key not found in manifest")
    }



    private fun setupMapAndStreetViewFragments() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        val streetViewFragment = supportFragmentManager.findFragmentById(R.id.streetviewpanorama) as? SupportStreetViewPanoramaFragment
        streetViewFragment?.getStreetViewPanoramaAsync(this)
    }

    private fun setupSlidingPanel() {
        slidingPanel = findViewById(R.id.sliding_layout)
        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN) // Ensure the panel is initially hidden

        slidingPanel.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
            }

            override fun onPanelStateChanged(panel: View, previousState: SlidingUpPanelLayout.PanelState, newState: SlidingUpPanelLayout.PanelState) {
            }
        })

        slidingPanel.setFadeOnClickListener {
            slidingPanel.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        }

        findViewById<Button>(R.id.buttonObterDirecoesPlaces).setOnClickListener {
            lastClickedMarker?.tag?.let { it as? Place }?.also { navigateToMapDirecoesActivity(it) }
        }

        findViewById<Button>(R.id.buttonAddFavPlaces).setOnClickListener {
            lastClickedMarker?.let { addPlaceToFavorites(it) }
        }

        findViewById<TextView>(R.id.textViewClosePanel).setOnClickListener {
            slidingPanel.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        }


    }


    @SuppressLint("MissingPermission")
    private fun setupMapFragment() {
        (supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        estacaoLocation = LatLng(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0))
        requestLocationPermission()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(estacaoLocation, 15f))
        setupMapListeners()
    }

    override fun onStreetViewPanoramaReady(panorama: StreetViewPanorama) {
        mStreetViewPanorama = panorama
        mStreetViewPanorama.setPosition(estacaoLocation, StreetViewSource.OUTDOOR)
        findViewById<FrameLayout>(R.id.streetviewpanorama).visibility = View.GONE
    }

    private fun setupStreetViewToggle() {
        findViewById<Button>(R.id.buttonToggleStreetView).setOnClickListener {
            toggleStreetView()
        }
    }

    @SuppressLint("CutPasteId")
    private fun toggleStreetView() {
        val streetViewVisibility = findViewById<FrameLayout>(R.id.streetviewpanorama).visibility
        findViewById<FrameLayout>(R.id.streetviewpanorama).visibility = if (streetViewVisibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            mMap.isMyLocationEnabled = true
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setupMapListeners() {
        mMap.setOnPoiClickListener { poi ->
            fetchPlaceDetails(poi.placeId)
        }
        mMap.setOnMarkerClickListener { marker ->
            (marker.tag as? Place)?.let {
                showPlaceDetails(it)
                true
            } ?: false
        }
    }

    private fun showPlaceDetails(place: Place) {
        mapInteractionJob?.cancel()
        mapInteractionJob = lifecycleScope.launch {
            updateUIWithPlaceDetails(place)
            mStreetViewPanorama.setPosition(place.latLng!!, StreetViewSource.OUTDOOR)
        }
    }


    private suspend fun updateUIWithPlaceDetails(place: Place) = withContext(Dispatchers.Main) {
        findViewById<TextView>(R.id.place_name).text = place.name
        findViewById<TextView>(R.id.place_street).text = place.address
        place.photoMetadatas?.firstOrNull()?.let { fetchPhotoAndDisplay(it, findViewById(R.id.place_image)) }
        slidingPanel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
    }


    private fun fetchPlaceDetails(placeId: String) {
        val request = FetchPlaceRequest.newInstance(placeId, listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS))
        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            lastClickedMarker = mMap.addMarker(MarkerOptions().position(place.latLng!!).title(place.name).snippet(place.address).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
            lastClickedMarker?.tag = place
            showPlaceDetails(place) // Directly show details
        }.addOnFailureListener { exception ->
            Log.e("fetchPlaceDetails", "Erro ao obter os locais: ", exception)
        }
    }


    private fun fetchPhotoAndDisplay(photoMetadata: PhotoMetadata, imageView: ImageView) {
        val request = FetchPhotoRequest.builder(photoMetadata).build()
        placesClient.fetchPhoto(request).addOnSuccessListener { fetchPhotoResponse ->
            imageView.setImageBitmap(fetchPhotoResponse.bitmap)
        }.addOnFailureListener {
            imageView.setImageResource(R.drawable.ic_launcher_background)
            Log.e("PoiMapActivity", "Erro ao obter a foto.")
        }
    }


    private fun navigateToMapDirecoesActivity(place: Place) {
        Intent(this, MapDirecoesActivity::class.java).also { intent ->
            intent.putExtra("latitude", place.latLng?.latitude)
            intent.putExtra("longitude", place.latLng?.longitude)
            startActivity(intent)
        }
        Log.d("PoiMapActivity", "A Navegar para MapDirecoesActivity com a localização: ${place.latLng}")
    }

    override fun onDestroy() {
        mapInteractionJob?.cancel()
        super.onDestroy()
    }

    private fun addPlaceToFavorites(marker: Marker) {
        val place = marker.tag as? Place ?: return
        val userId = SessionManager.userId?.toInt() ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val pontoInteresseDao = db.pontoInteressedao()
            val favoritoDao = db.favoritoDao()

            val placeLatLng = place.latLng
            if (placeLatLng != null) {
                val existingPontoInteresse = pontoInteresseDao.getPontoInteresseByNomeECoordenadas(place.name, placeLatLng.latitude, placeLatLng.longitude)
                val pontoInteresseId = existingPontoInteresse?.id ?: pontoInteresseDao.insertPontoInteresse(Ponto_interesse(0, place.name, placeLatLng.latitude, placeLatLng.longitude)).toInt()

                val isAlreadyFavorite = favoritoDao.getFavoritoByUtilizadorIdEPontoInteresse(userId, pontoInteresseId) != null
                if (!isAlreadyFavorite) {
                    favoritoDao.addFavorito(Favorito(0, null, pontoInteresseId, userId))
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Ponto de interesse adicionado aos favoritos", Toast.LENGTH_LONG).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Ponto de interesse já pertence aos favoritos", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
            }
        } else {
            Toast.makeText(this, "Permissão recusada", Toast.LENGTH_SHORT).show()
        }
    }
}
