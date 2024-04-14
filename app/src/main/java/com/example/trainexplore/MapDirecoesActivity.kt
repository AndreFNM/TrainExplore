package com.example.trainexplore

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.provider.Settings
import android.util.Log
import android.widget.Button
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MapDirecoesActivity : AppCompatActivity(), OnMapReadyCallback {
    private var  map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    companion object{
        private const val LOCATION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.direcoes_map_activity)

        // Initialize location services components
        createLocationRequest()
        createLocationCallback()

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Request location permissions
        requestPermissaoLocalizacao()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMap()
    }

    private fun setupMap() {
        map?.let { safeMap ->
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_REQUEST_CODE)
                return
            }
            safeMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLocation = LatLng(it.latitude, it.longitude)
                    val destinationLatLng = LatLng(intent.getDoubleExtra("latitude", 0.0), intent.getDoubleExtra("longitude", 0.0))
                    calcularRota(userLocation, destinationLatLng)
                } ?: Toast.makeText(this, "Localização atual não disponível.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun calcularRota(origem: LatLng, destino: LatLng) {
        fetchRoute(origem, destino)
        map?.let { safemap ->
            safemap.addMarker(MarkerOptions().position(destino).title("Estacao"))
            safemap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 15f))

            val bounds = LatLngBounds.Builder()
                .include(origem)
                .include(destino)
                .build()
            val padding = 100
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            safemap.animateCamera(cameraUpdate)
        }
    }

    private fun fetchRoute(origin: LatLng, destination: LatLng) {
        val apiKey = getApiKey()
        val travelMode = "driving"
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}&" +
                "destination=${destination.latitude},${destination.longitude}&" +
                "mode=driving&" +
                "key=$apiKey"

        Log.d("MapDirecoesActivity","Requesting route with url: $url")

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MapDirecoesActivity, "Falha ao encontrar uma rota: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use { resp ->
                    if (!resp.isSuccessful) {
                        val errorBody = resp.body?.string() ?: "No details"
                        Log.d("MapDirecoesActivity","Response failed with code ${resp.code} and message $errorBody")
                        runOnUiThread {
                            Toast.makeText(this@MapDirecoesActivity, "Resposta sem sucesso: ${resp.message} - $errorBody", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val jsonData = resp.body?.string()
                        Log.d("MapDirecoesActivity","Response Body: $jsonData")
                        jsonData?.let {
                            try {
                                val jsonObject = JSONObject(it)
                                val routes = jsonObject.getJSONArray("routes")
                                if (routes.length() > 0) {
                                    val polyline = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
                                    val decodedPath = PolyUtil.decode(polyline)
                                    runOnUiThread {
                                        map?.addPolyline(PolylineOptions().addAll(decodedPath).color(android.graphics.Color.RED).width(8f))
                                    }
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(this@MapDirecoesActivity, "Nenhuma rota encontrada", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                                runOnUiThread {
                                    Toast.makeText(this@MapDirecoesActivity, "Erro ao processar a rota: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun checkServicosLocalizacaoERequestLocalizacao() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGPSEnabled && !isNetworkEnabled) {
            promptParaServicosLocalizacao()
        } else {
            requestPermissaoLocalizacao()
        }
    }

    private fun promptParaServicosLocalizacao() {
        AlertDialog.Builder(this)
            .setMessage("Serviçoes de Localização desativados. Por favor, ative os serviçoes de localização para utilizar o mapa")
            .setPositiveButton("Definições") {_, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancelar",null)
            .show()
    }

    private fun promptParaAbrirDefinicoes() {
        AlertDialog.Builder(this)
            .setMessage("Permissões de localização é necessária para esta funcionalidade. Por favor ative nas definições da aplicação.")
            .setPositiveButton("abra as definições") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package",packageName, null))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .setNegativeButton("Cancelar",null)
            .show()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000  // Update location every 10 seconds
            fastestInterval = 5000  // Maximum rate at which your app can handle updates
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }


    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                }
            }
        }
    }

    private fun requestPermissaoLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_REQUEST_CODE)
        } else {
            enableLocalizacao()
        }
    }

    override fun onResume() {
        super.onResume()
        checkServicosLocalizacaoERequestLocalizacao()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            //se as permissões não forem garantidas
            requestPermissaoLocalizacao()
        }
    }

    private fun stopLocationUpdates() {
        if (this::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun enableLocalizacao() {
        map?.let {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                it.isMyLocationEnabled = true
                fusedLocationClient.lastLocation.addOnSuccessListener { localizacao ->
                    if (localizacao != null) {
                        val userLocation = LatLng(localizacao.latitude, localizacao.longitude)
                        it.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
                    } else {
                        Toast.makeText(this,"Localização não está disponível", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocalizacao()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "Permissão recusada. Por favor ativa nas definicões da aplicação",Toast.LENGTH_LONG).show()
                    promptParaServicosLocalizacao()
                }else {
                    Toast.makeText(this, "Permissão recusada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun getApiKey(): String {
        try {
            val applicationInfo = this.packageManager.getApplicationInfo(this.packageName, PackageManager.GET_META_DATA)
            val bundle = applicationInfo.metaData
            return bundle.getString("com.google.android.geo.API_KEY") ?: throw IllegalStateException("API key não encontrada em android manifest")
        } catch (e: PackageManager.NameNotFoundException) {
            throw IllegalStateException("Falha a dar load as dados, NomeNaoEncontrado: ${e.message}")
        } catch (e: NullPointerException) {
            throw IllegalStateException("Falha a dar load as dados, PonteiroNull: ${e.message}")
        }
    }




}