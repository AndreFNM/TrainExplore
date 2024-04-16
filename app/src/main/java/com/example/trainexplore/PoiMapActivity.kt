package com.example.trainexplore


import PlacesAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.example.trainexplore.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.lang.reflect.Field


class PoiMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RecyclerView>
    private lateinit var poiAdapter: PlacesAdapter
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi_map)

        val apiKey = getApiKey()  // Correctly retrieving the API key
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
            placesClient = Places.createClient(this)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.poiMap) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        setupRecyclerView()
    }

    private fun getApiKey(): String {
        val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return applicationInfo.metaData.getString("com.google.android.geo.API_KEY") ?: throw IllegalStateException("API key not found in manifest")
    }



    private fun setupRecyclerView() {
        val poiList = findViewById<RecyclerView>(R.id.poi_list)
        poiAdapter = PlacesAdapter()
        poiList.adapter = poiAdapter
        poiList.layoutManager = LinearLayoutManager(this)
        bottomSheetBehavior = BottomSheetBehavior.from(poiList).apply {
            peekHeight = 300
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Setup the initial view of the map
        val stationLocation = LatLng(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0))
        map.addMarker(MarkerOptions().position(stationLocation).title("Station"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(stationLocation, 15f))

        // Set a marker click listener
        map.setOnMarkerClickListener { marker ->
            // You could retrieve and show details about the place here
            false // Return false to show the default info window
        }

        // Set custom info window adapter
        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? = null // Use default frame

            @SuppressLint("InflateParams")
            override fun getInfoContents(marker: Marker): View {
                // Inflate and return a custom view here
                val view = layoutInflater.inflate(R.layout.place_item_view, null)
                val textView: TextView = view.findViewById(R.id.placeName)
                textView.text = marker.title // Set the marker's title as text
                return view
            }
        })

        // Load places of interest around the station
        loadNearbyPlaces(stationLocation)
    }

    private fun loadNearbyPlaces(location: LatLng) {
        // Define a query to search for coffee shops or other categories
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            placesClient.findCurrentPlace(request).addOnSuccessListener { response ->
                for (placeLikelihood in response.placeLikelihoods) {
                    val place = placeLikelihood.place
                    map.addMarker(MarkerOptions().position(place.latLng ?: location).title(place.name))
                }
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    Log.e("PlacesAPI", "Place not found: ${exception.statusCode}")
                }
            }
        } else {
            // Request das permissoes em falta
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
