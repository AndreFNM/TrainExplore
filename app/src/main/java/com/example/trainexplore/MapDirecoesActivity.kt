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
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.os.Looper
import android.view.WindowManager
import android.widget.RadioGroup
import android.widget.TextView
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.tasks.Task
import com.google.maps.android.SphericalUtil
import android.text.Html


data class NavigationStep(
    val instruction: String,
    val distance: String,
    val polyline: List<LatLng>,
    val travelMode: String? = null,
    val transitDetails: TransitDetails? = null
)

data class TransitDetails(
    val departureStop: String,
    val arrivalStop: String,
    val vehicleType: String,
    val lineName: String,
    val headsign: String,
    val numStops: Int
)


class MapDirecoesActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var routePolyline: Polyline? = null
    private var currentPolyline: List<LatLng>? = null
    private var selectedTravelMode = "driving"
    private var currentStepIndex = 0
    private var steps = mutableListOf<NavigationStep>()

    companion object {
        private const val LOCATION_REQUEST_CODE = 101
        private const val REQUEST_CHECK_SETTINGS = 0x1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.direcoes_map_activity)

        val modeSelector = findViewById<RadioGroup>(R.id.modeSelector)
        modeSelector.setOnCheckedChangeListener { _, checkedId ->
            selectedTravelMode = when (checkedId) {
                R.id.drivingMode -> "driving"
                R.id.walkingMode -> "walking"
                R.id.transitMode -> "transit"
                else -> "driving"
            }
            saveTravelMode(selectedTravelMode)
            updateRouteBasedOnMode()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        createLocationRequest()
        createLocationCallback()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkLocationSettings()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
        }
    }

    private fun updateRouteBasedOnMode() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    val destinationLatLng = LatLng(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0))
                    // Clear the existing route before fetching a new one
                    clearExistingPolyline()
                    fetchRoute(userLatLng, destinationLatLng)
                } ?: Toast.makeText(this, "Localização atual não disponível.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Permissões de localização não garantidas", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearExistingPolyline() {
        routePolyline?.remove()
        routePolyline = null
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
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            } else {
                Toast.makeText(this, "Permissões de localização não garantidas", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Falha ao pedir atualizações da localização: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateRoute(location) // Assegurar que a rota é atualizada após a atualização da localização do utilizador
                }
            }
        }
    }

    private fun updateRoute(location: Location) {
        val userLocation = LatLng(location.latitude, location.longitude)
        checkForNextStep(userLocation)
    }

    private fun requestLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                updateRoute(it)
            } ?: Toast.makeText(this, "Ultima localizão não disponível", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkRouteDeviation(userLocation: LatLng) {
        routePolyline?.let {
            // Verificar se a localição está no caminho atual
            if (!PolyUtil.isLocationOnPath(userLocation, it.points, true, 100.0)) {
                val destinationLatLng = LatLng(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0))
                fetchRoute(userLocation, destinationLatLng)
                Toast.makeText(this, "A Recalcular a rota...", Toast.LENGTH_SHORT).show()
            } else {
                updatePolyline(userLocation)
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
        Toast.makeText(this, "Mapa clicado em: $point", Toast.LENGTH_SHORT).show()
    }




    private fun displayRouteOnMap(route: List<LatLng>) {
        clearExistingPolyline()  // Assegurar que a rota anterior é removida
        routePolyline = map?.addPolyline(PolylineOptions().addAll(route).color(android.graphics.Color.RED).width(8f))
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

    private fun buildDirectionsUrl(origin: LatLng, destination: LatLng): String {
        val originParam = "origin=${origin.latitude},${origin.longitude}"
        val destinationParam = "destination=${destination.latitude},${destination.longitude}"
        val modeParam = "mode=$selectedTravelMode"
        val languageParam = "language=pt-PT"
        val apiKeyParam = "key=${getApiKey()}"
        return "https://maps.googleapis.com/maps/api/directions/json?$originParam&$destinationParam&$modeParam&$languageParam&$apiKeyParam"
    }


    private fun fetchRoute(origin: LatLng, destination: LatLng) {
        val url = buildDirectionsUrl(origin, destination)
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread { Toast.makeText(this@MapDirecoesActivity, "Falha ao obter a rota: ${e.message}", Toast.LENGTH_LONG).show() }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use { resp ->
                    if (resp.isSuccessful) {
                        val jsonData = resp.body?.string()
                        jsonData?.let { parseDirections(it) }
                    } else {
                        runOnUiThread { Toast.makeText(this@MapDirecoesActivity, "Falha ao obter a rota", Toast.LENGTH_LONG).show() }
                    }
                }
            }
        })
    }

    private fun showNextStep() {
        if (currentStepIndex < steps.size) {
            val currentStep = steps[currentStepIndex]
            val transitInfo = currentStep.transitDetails
            if (transitInfo != null) {
                findViewById<TextView>(R.id.navigationInstructions).text = buildString {
                    append("${currentStep.instruction} - Apanhe o ${transitInfo.lineName} em direção a ${transitInfo.headsign}.")
                    append("\nDe ${transitInfo.departureStop} para ${transitInfo.arrivalStop}, ${transitInfo.numStops} Paragens.")
                }
            } else {
                findViewById<TextView>(R.id.navigationInstructions).text = "${currentStep.instruction} in ${currentStep.distance}"
            }
            currentStepIndex++
        }
    }



    private fun parseDirections(jsonData: String) {
        val jsonObject = JSONObject(jsonData)
        val routes = jsonObject.getJSONArray("routes")
        if (routes.length() > 0) {
            val route = routes.getJSONObject(0)
            val legs = route.getJSONArray("legs")
            steps.clear()
            val path = ArrayList<LatLng>()

            for (i in 0 until legs.length()) {
                val leg = legs.getJSONObject(i)
                val legSteps = leg.getJSONArray("steps")
                for (j in 0 until legSteps.length()) {
                    val step = legSteps.getJSONObject(j)
                    val instruction = Html.fromHtml(step.getString("html_instructions")).toString()
                    val distance = step.getJSONObject("distance").getString("text")
                    val polyline = step.getJSONObject("polyline").getString("points")
                    val decodedPath = PolyUtil.decode(polyline)
                    val travelMode = step.getString("travel_mode")

                    var transitDetails: TransitDetails? = null
                    if (travelMode == "TRANSIT") {
                        val transitInfo = step.getJSONObject("transit_details")
                        transitDetails = TransitDetails(
                            departureStop = transitInfo.getJSONObject("departure_stop").getString("name"),
                            arrivalStop = transitInfo.getJSONObject("arrival_stop").getString("name"),
                            vehicleType = transitInfo.getJSONObject("line").getJSONObject("vehicle").getString("type"),
                            lineName = transitInfo.getJSONObject("line").getString("short_name"),
                            headsign = transitInfo.getString("headsign"),
                            numStops = transitInfo.getInt("num_stops")
                        )
                    }

                    steps.add(NavigationStep(instruction, distance, decodedPath, travelMode, transitDetails))
                    path.addAll(decodedPath)
                }
            }
            runOnUiThread {
                displayRouteOnMap(path)
                if (steps.isNotEmpty()) {
                    showNextStep()
                }
            }
        }
    }




    private fun checkForNextStep(userLocation: LatLng) {
        if (currentStepIndex < steps.size && nearNextStep(userLocation, steps[currentStepIndex])) {
            showNextStep()
        }
    }


    private fun nearNextStep(userLocation: LatLng, step: NavigationStep): Boolean {
        val nextStepLocation = step.polyline.last()
        val distance = SphericalUtil.computeDistanceBetween(userLocation, nextStepLocation)
        return distance < 50  // Verificar se está a menos de 50 metros do próximo passo
    }

    private fun getApiKey(): String {
        return packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData.getString("com.google.android.geo.API_KEY")
            ?: throw IllegalStateException("API key não encontrada no manifest")
    }

    private fun calcularRota(origem: LatLng, destino: LatLng) {
        fetchRoute(origem, destino)
        map?.let { safeMap ->
            safeMap.addMarker(MarkerOptions().position(destino).title("Destino"))
            safeMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 15f))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSettings()
            } else {
                Toast.makeText(this, "Permissão negada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Permissões não garatidas mas mostrar-se a resolução ao utilizador com um dialog
                try {
                    exception.startResolutionForResult(this@MapDirecoesActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignorar este erro
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                if (resultCode == RESULT_OK) {
                    startLocationUpdates()
                } else {
                    Toast.makeText(this, "Localização não está ativada, o Utilizador cancelou.", Toast.LENGTH_LONG).show()
                }
            }
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


    private fun updatePolyline(userLocation: LatLng) {
        routePolyline?.let { polyline ->
            val points = polyline.points
            val closestIndex = getClosestIndex(points, userLocation)
            if (closestIndex > 0) {
                val updatedPoints = points.subList(closestIndex, points.size)
                polyline.points = updatedPoints
            }
        }
    }

    private fun getClosestIndex(points: List<LatLng>, userLocation: LatLng): Int {
        var closestDistance = Double.MAX_VALUE
        var closestIndex = 0

        for (index in points.indices) {
            val point = points[index]
            val distance = SphericalUtil.computeDistanceBetween(point, userLocation)
            if (distance < closestDistance) {
                closestDistance = distance
                closestIndex = index
            }
        }
        return closestIndex
    }

    private fun checkIfArrived(destination: LatLng, currentLocation: LatLng) {
        val distance = SphericalUtil.computeDistanceBetween(currentLocation, destination)
        if (distance < 50) { // aos 50 metros de proximidade considera que o utilizador chegou ao local.
            notifyUserArrived()
        }
    }

    private fun notifyUserArrived() {
        Toast.makeText(this, "Chegou ao Destino!", Toast.LENGTH_LONG).show()
        stopLocationUpdates()
    }

    private fun saveTravelMode(mode: String) {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("TravelMode", mode)
        editor.apply()
    }


}
