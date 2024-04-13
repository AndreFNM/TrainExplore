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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.provider.Settings
class MapDirecoesActivity : AppCompatActivity(), OnMapReadyCallback {
    private var  map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableLocalizacao()
        } else {
            requestPermissaoLocalizacao()
        }
        /**
        val latitude = intent.getDoubleExtra("latitude",0.0)
        val longitude = intent.getDoubleExtra("longitude",0.0)
        val localizacaoEstacao = LatLng(latitude, longitude)

        map.addMarker(MarkerOptions().position(localizacaoEstacao).title("Estacao"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacaoEstacao, 15f))
        **/
    }
}