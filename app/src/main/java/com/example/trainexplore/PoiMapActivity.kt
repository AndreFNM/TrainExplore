package com.example.trainexplore

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.bumptech.glide.Glide
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.net.FetchPhotoRequest

class PoiMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var estacaoLocation: LatLng
    private var lastClickedMarker: Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poi_map)
        initializePlaces()
        setUpMapFragment()
    }

    private fun initializePlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext,"AIzaSyCTs0K1kW7N2QzvknvUU40btJLdk6DXB9w")
        }
        placesClient = Places.createClient(this)
    }

    private fun setUpMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    @SuppressLint("MissingPermission", "PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        estacaoLocation = LatLng(
            intent.getDoubleExtra("latitude", 0.0),
            intent.getDoubleExtra("longitude", 0.0)
        )

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(estacaoLocation, 15f))
        mMap.setInfoWindowAdapter(this)
        mMap.setOnPoiClickListener { poi ->
            val placeId = poi.placeId
            fetchPlaceDetails(placeId)
        }
    }


    override fun getInfoWindow(marker: Marker): View {
        val infoWindow = layoutInflater.inflate(R.layout.place_info_window, null)
        renderInfoWindowContents(marker, infoWindow)
        return infoWindow
    }

    private fun renderInfoWindowContents(marker: Marker, infoWindow: View) {
        val title = infoWindow.findViewById<TextView>(R.id.info_window_title)
        val address = infoWindow.findViewById<TextView>(R.id.info_window_address)
        val image = infoWindow.findViewById<ImageView>(R.id.info_window_image)

        title.text = marker.title
        address.text = marker.snippet

        // Handle the bitmap from the tag, if it's a Bitmap object.
        val bitmap = marker.tag as? Bitmap
        if (bitmap != null) {
            image.setImageBitmap(bitmap)
        } else {
            // Here, you might set a default image or handle the case when there's no image.
            image.setImageResource(R.drawable.ic_launcher_background)  // Replace with your default image.
        }
    }


    override fun getInfoContents(marker: Marker): View? {
        // This method is not used in this case but must be overridden
        return null
    }

    // Helper method to render the info window contents


    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS)
        val fetchPlaceRequest = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener { response ->
            val place = response.place
            Log.i("PlaceDetails", "${place.name}, ${place.address}, ${place.latLng}")

            val markerOptions = MarkerOptions()
                .position(place.latLng!!)
                .title(place.name)
                .snippet(place.address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

            // Create a marker and store it in a variable so you can set the tag later.
            val marker = mMap.addMarker(markerOptions)
            marker?.tag = null  // Set the tag to null initially.
            marker?.showInfoWindow()

            // Get the photo metadata and fetch the bitmap
            place.photoMetadatas?.firstOrNull()?.let { photoMetadata ->
                val photoRequest = FetchPhotoRequest.builder(photoMetadata).build()
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener { fetchPhotoResponse ->
                    val bitmap = fetchPhotoResponse.bitmap
                    marker?.tag = bitmap  // Now, set the tag of the marker to the fetched bitmap.
                    updateInfoWindow(marker)  // Call this method to refresh the info window.
                }.addOnFailureListener { exception ->
                    Log.e("PlacePhotos", "Photo not found: ${exception.localizedMessage}")
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("PlaceDetails", "Place not found: ${exception.localizedMessage}")
        }
    }

    private fun updateInfoWindow(marker: Marker?) {
        // Only update the info window if the marker is not null and the info window is already shown.
        if (marker != null && marker.isInfoWindowShown) {
            marker.showInfoWindow()
        }
    }





    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

}
