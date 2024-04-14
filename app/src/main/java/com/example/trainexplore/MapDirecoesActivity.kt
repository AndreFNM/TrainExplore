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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.direcoes_map_activity)

        // inicializar FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Pedir permissões
        requestPermissaoLocalizacao()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        fusedLocationClient.removeLocationUpdates(locationCallback)
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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMap()
    }

    private fun setupMap() {
        map?.let { safemap ->
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                safemap.isMyLocationEnabled = true
                enableLocalizacao()
            } else {
                requestPermissaoLocalizacao()
            }

            val destinoLatitude = intent.getDoubleExtra("latitude", 0.0)
            val destinoLongitude = intent.getDoubleExtra("longitude", 0.0)
            val destinoLocalizacao = LatLng(destinoLatitude, destinoLongitude)

            safemap.addMarker(MarkerOptions().position(destinoLocalizacao).title("Estacao"))
            safemap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinoLocalizacao,15f))

            calcularRota(destinoLocalizacao)
        }
    }


    private fun fetchRoute(origin: LatLng, destino: LatLng) {
        val url = "https://api.example.com/route?origin=${origin.latitude},${origin.longitude}&destination=${destino.latitude},${destino.longitude}&key=YOUR_API_KEY"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MapDirecoesActivity, "Falha ao calcular a rota: ${e.message}",Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use { resp ->
                    if (!resp.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@MapDirecoesActivity, "Resposta sem sucesso", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        val jsonData = resp.body?.string()
                        jsonData?.let {
                            val jsonObject = JSONObject(it)
                            val polyline = jsonObject.getJSONArray("rotas").getJSONObject(0).getJSONObject("overview_polyline").getString("pontos")
                            val decodedPath = PolyUtil.decode(polyline)
                            runOnUiThread {
                                map?.addPolyline(PolylineOptions().addAll(decodedPath).color(android.graphics.Color.RED).width(8f))
                            }
                        }
                    }
                }
            }
        } )
    }

    private fun calcularRota(destino: LatLng) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { localizacao ->
                localizacao?.let {
                    val origin = LatLng(it.latitude, it.longitude)
                    fetchRoute(origin, destino)
                }
            }
        } else {
            requestPermissaoLocalizacao()
        }
    }
}