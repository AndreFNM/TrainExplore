package com.example.trainexplore.database.PoiDB

import com.google.android.gms.maps.model.LatLng

data class PlaceData(val id: Int, val nome: String, val localizacao: LatLng)
