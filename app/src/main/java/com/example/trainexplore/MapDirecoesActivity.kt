package com.example.trainexplore

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper

class MapDirecoesActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var routePolyline: Polyline? = null
    private var currentPolyline: List<LatLng>? = null

    companion object {
        private const val LOCATION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.direcoes_map_activity)

        createLocationRequest()
        createLocationCallback()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    //verificar constantemente se o utilizador está a mudar de direção (startLocationUpdates e createLocationCallback)
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    checkRouteDeviation(userLocation)
                }
            }
        }
    }


    private fun checkRouteDeviation(userLocation: LatLng) {
        routePolyline?.let {
            if (!PolyUtil.isLocationOnPath(userLocation, it.points, true, 100.0)) {
                val destinationLatLng = LatLng(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0))
                fetchRoute(userLocation, destinationLatLng)
                Toast.makeText(this, "A Recalcular a rota...", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @SuppressLint("PotentialBehaviorOverride")
    private fun setupMap() {
        map?.let { safeMap ->
            safeMap.setOnMarkerClickListener(this)
            safeMap.setOnMapClickListener(this)

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
                return
            }
            safeMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLocation = LatLng(it.latitude, it.longitude)
                    val destinationLatLng = LatLng(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0))
                    val markerOptions = MarkerOptions().position(destinationLatLng).draggable(true)
                    safeMap.addMarker(markerOptions)

                    calcularRota(userLocation, destinationLatLng)

                    safeMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                        override fun onMarkerDragStart(marker: Marker) {}
                        override fun onMarkerDrag(marker: Marker) {}
                        override fun onMarkerDragEnd(marker: Marker) {
                            calcularRota(userLocation, marker.position)
                        }
                    })
                } ?: Toast.makeText(this, "Localização atual não disponível.", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMap()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        return false
    }

    override fun onMapClick(point: LatLng) {
        Toast.makeText(this, "Map clicked at: $point", Toast.LENGTH_SHORT).show()
    }


    private fun displayRouteOnMap() {
        val localPolyline = currentPolyline?.toList()

        localPolyline?.let { safePolyline ->
            map?.addPolyline(
                PolylineOptions()
                    .addAll(safePolyline)
                    .color(android.graphics.Color.RED)
                    .width(8f)
            )
        }
    }


    private fun shouldRecalculateRoute(currentLocation: Location): Boolean {
        val currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val polyline = currentPolyline?.toList()

        return if (polyline != null) {
            !PolyUtil.isLocationOnPath(currentLatLng, polyline, true, 100.0)
        } else {
            true
        }
    }
    private fun fetchRoute(origin: LatLng, destination: LatLng) {
        val apiKey = getApiKey()
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}&" +
                "destination=${destination.latitude},${destination.longitude}&" +
                "key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread { Toast.makeText(this@MapDirecoesActivity, "Route fetch failed: ${e.message}", Toast.LENGTH_LONG).show() }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use { resp ->
                    if (resp.isSuccessful) {
                        val jsonData = resp.body?.string()
                        jsonData?.let {
                            val jsonObject = JSONObject(it)
                            val routes = jsonObject.getJSONArray("routes")
                            if (routes.length() > 0) {
                                val polyline = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
                                currentPolyline = PolyUtil.decode(polyline)
                                runOnUiThread {
                                    routePolyline?.remove()
                                    routePolyline = map?.addPolyline(PolylineOptions().addAll(currentPolyline!!).color(android.graphics.Color.RED).width(8f))
                                    Toast.makeText(this@MapDirecoesActivity, "New route displayed.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        runOnUiThread { Toast.makeText(this@MapDirecoesActivity, "Failed to fetch route", Toast.LENGTH_LONG).show() }
                    }
                }
            }
        })
    }





    private fun getApiKey(): String {
        return packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData.getString("com.google.android.geo.API_KEY")
            ?: throw IllegalStateException("API key not found in manifest")
    }

    private fun calcularRota(origem: LatLng, destino: LatLng) {
        fetchRoute(origem, destino)
        map?.let { safeMap ->
            safeMap.addMarker(MarkerOptions().position(destino).title("Destination"))
            safeMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 15f))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupMap()
        } else {
            Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        if (this::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

}
