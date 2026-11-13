package com.smi.locatioandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.smi.locatioandroid.screens.MapScreen
import com.smi.locatioandroid.ui.theme.LocatioAndroidTheme
import com.smi.locatioandroid.utils.ManifestUtils
import com.smi.locatioandroid.viewmodel.MapViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recuperar la clave API del archivo de manifiesto
        val apiKey = ManifestUtils.getApiKeyFromManifest( this )
        // Inicializar la API de Places con la clave API recuperada
        if (!Places.isInitialized() && apiKey != null ) {
            Places.initialize(applicationContext, apiKey)
        }

        enableEdgeToEdge()
        setContent {
            LocatioAndroidTheme {
                val mapViewModel = MapViewModel()
                MapScreen(mapViewModel)
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    MiMapa(modifier = Modifier.padding(innerPadding))
//                }
            }
        }
    }
}

@Composable
fun MiMapa(modifier: Modifier){

    val posicionGuardada = rememberSaveable {
        LatLng(-18.006077, -70.226528)
    }

    val markerState = rememberMarkerState(
        position = posicionGuardada
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(posicionGuardada, 10f)
    }

    val properties by remember{ mutableStateOf(MapProperties(mapType = MapType.SATELLITE)) }
    val uiSettings by remember{ mutableStateOf(MapUiSettings(zoomControlsEnabled = false,
        myLocationButtonEnabled = true)) }

    GoogleMap(modifier = Modifier.fillMaxSize(),
        properties = properties,
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        onMapLongClick = { latLng ->
            println("Long press en: $latLng")
        }) {
        Marker(state = markerState, title = "Mi Universidad")
    }
}