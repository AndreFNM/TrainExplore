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
import android.util.Log
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
    val duration: String,
    val durationInSecs: Int,
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
            interval = 3000  // Intervalo de verificação reduzido para 3 segundos
            fastestInterval = 1000  // Intervalo mais rápido reduzido para 1 segundo
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            Toast.makeText(this, "Permissões de localização não garantidas", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { location ->
                    updateRoute(location)
                    animateCameraToLocation(location)
                    checkRouteDeviation(LatLng(location.latitude, location.longitude)) // Verificar desvio imediatamente
                }
            }
        }
    }

    private fun animateCameraToLocation(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        if (location.hasBearing()) {
            val cameraPosition = CameraPosition.builder()
                .target(userLatLng)
                .zoom(18f)  // Aumentar o zoom para 18
                .bearing(location.bearing)
                .build()
            map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null) // Animação suave de 1 segundo
        } else {
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f), 1000, null) // Animação suave de 1 segundo
        }
    }

    private fun updateRoute(location: Location) {
        val userLocation = LatLng(location.latitude, location.longitude)
        checkForNextStep(userLocation)
        updatePolyline(userLocation)
        checkIfArrived(LatLng(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0)), userLocation)
    }

    private fun requestLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { updateRoute(it) }
                    ?: Toast.makeText(this, "Ultima localizão não disponível", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkRouteDeviation(userLocation: LatLng) {
        routePolyline?.let { polyline ->
            if (!PolyUtil.isLocationOnPath(userLocation, polyline.points, true, 100.0)) {
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

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMap()
    }

    override fun onMarkerClick(marker: Marker): Boolean = false

    override fun onMapClick(point: LatLng) {
        Toast.makeText(this, "Mapa clicado em: $point", Toast.LENGTH_SHORT).show()
    }

    fun displayRouteOnMap(route: List<LatLng>) {
        clearExistingPolyline()
        routePolyline = map?.addPolyline(PolylineOptions().addAll(route).color(android.graphics.Color.RED).width(8f))
        currentPolyline = route
    }

    private fun shouldRecalculateRoute(currentLocation: Location): Boolean {
        val currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        return currentPolyline?.let { !PolyUtil.isLocationOnPath(currentLatLng, it.toList(), true, 100.0) } ?: true
    }

    private fun buildDirectionsUrl(origin: LatLng, destination: LatLng): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&mode=$selectedTravelMode&language=pt-PT&key=${getApiKey()}"
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
                        resp.body?.string()?.let { parseDirections(it) }
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
            findViewById<TextView>(R.id.navigationInstructions).text = formatInstruction(currentStep)
            currentStepIndex++
        }
    }

    private fun formatInstruction(step: NavigationStep): String {
        step.transitDetails?.let {
            return "${step.instruction} - Apanhe o ${it.lineName} em direção a ${it.headsign}.\nDe ${it.departureStop} para ${it.arrivalStop}, ${it.numStops} Paragens."
        }
        return "${step.instruction} in ${step.distance}"
    }

    fun parseDirections(jsonData: String) {
        try {
            val jsonObject = JSONObject(jsonData)
            val routes = jsonObject.getJSONArray("routes")
            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val legs = route.getJSONArray("legs")
                steps.clear()
                var totalDuration = 0L
                val path = ArrayList<LatLng>()

                for (i in 0 until legs.length()) {
                    val leg = legs.getJSONObject(i)
                    totalDuration += leg.getJSONObject("duration").getLong("value")
                    val legSteps = leg.getJSONArray("steps")

                    for (j in 0 until legSteps.length()) {
                        val step = legSteps.getJSONObject(j)
                        val instruction = Html.fromHtml(step.getString("html_instructions")).toString()
                        val distance = step.getJSONObject("distance").getString("text")
                        val duration = step.getJSONObject("duration").getString("text")
                        val durationSecs = step.getJSONObject("duration").getInt("value")
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

                        steps.add(NavigationStep(instruction, distance, duration, durationSecs, decodedPath, travelMode, transitDetails))
                        path.addAll(decodedPath)
                    }
                }
                runOnUiThread {
                    displayRouteOnMap(path)
                    findViewById<TextView>(R.id.estimatedTime).text = "Tempo Estimado: ${formatDuration(totalDuration)}"
                    if (steps.isNotEmpty()) {
                        showNextStep()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MapDirecoesActivity", "Failed to parse directions: ${e.message}")
        }
    }

    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return String.format("%dh %02dm", hours, minutes)
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
            safeMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 18f)) // Ajuste do zoom para 18
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
            interval = 3000  // Intervalo de verificação reduzido para 3 segundos
            fastestInterval = 1000  // Intervalo mais rápido reduzido para 1 segundo
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
                // Permissões não garantidas mas mostrar-se a resolução ao utilizador com um dialog
                try {
                    exception.startResolutionForResult(this@MapDirecoesActivity, REQUEST_CHECK_SETTINGS)
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
                    Toast.makeText(this, "Localização não está ativada, o utilizador cancelou.", Toast.LENGTH_LONG).show()
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
        try {
            routePolyline?.let { polyline ->
                val points = polyline.points
                val closestIndex = getClosestIndex(points, userLocation)
                if (closestIndex > 0) {
                    val updatedPoints = points.subList(closestIndex, points.size)
                    polyline.points = updatedPoints

                    val remainingTime = steps.subList(currentStepIndex, steps.size).sumOf { step ->
                        step.durationInSecs.toLong()
                    }
                    findViewById<TextView>(R.id.estimatedTime).text = "Tempo restante: ${formatDuration(remainingTime)}"
                    currentStepIndex = closestIndex
                }
            }
        } catch (e: Exception) {
            Log.e("MapDirecoesActivity", "Failed to update polyline: ${e.message}")
        }
    }

    private fun getClosestIndex(points: List<LatLng>, userLocation: LatLng): Int {
        return points.indices.minByOrNull { index ->
            SphericalUtil.computeDistanceBetween(points[index], userLocation)
        } ?: 0
    }

    private fun checkIfArrived(destination: LatLng, currentLocation: LatLng) {
        val distance = SphericalUtil.computeDistanceBetween(currentLocation, destination)
        if (distance < 30) { // aos 30 metros de proximidade considera que o utilizador chegou ao local.
            notifyUserArrived()
        }
    }

    private fun notifyUserArrived() {
        AlertDialog.Builder(this)
            .setTitle("Notificação de chegada")
            .setMessage("Chegou ao Destino!")
            .setPositiveButton("OK") { dialog, which ->
                stopLocationUpdates()
            }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }

    private fun saveTravelMode(mode: String) {
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("TravelMode", mode)
        editor.apply()
    }
}
