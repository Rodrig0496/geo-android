package com.smi.locatioandroid.screens

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.smi.locatioandroid.viewmodel.MapViewModel
import timber.log.Timber

@Composable
fun MapScreen(mapViewModel: MapViewModel){
    // Coordenadas de la universidad
    val universidadLocation = remember { LatLng(-18.006077, -70.226528) }

    // Inicializa el estado de posición de la cámara centrado en la universidad
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(universidadLocation, 15f)
    }
    // Obtiene el contexto actual
    val context = LocalContext.current
    // Observa la ubicación del usuario desde el ViewModel
    val userLocation by mapViewModel.userLocation
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Observa los lugares cercanos de Google Places API
    val nearbyPlaces by mapViewModel.nearbyPlaces
    val isLoadingPlaces by mapViewModel.isLoadingPlaces

    // Manejar las solicitudes de permiso para acceder a la ubicación precisa
    val permissionLauncher = rememberLauncherForActivityResult (
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Obtener la ubicación del usuario si se otorga el permiso
            mapViewModel.fetchUserLocation(context, fusedLocationClient)
        } else {
            // Manejar el caso en que se deniega el permiso
            Timber.e("El usuario denegó el permiso de ubicación.")
        }
    }

    // Observa la ubicación seleccionada desde el ViewModel
    val selectedLocation by mapViewModel.selectedLocation

    // Solicitar el permiso de ubicación cuando se lanza el componible
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            // Comprobar si el permiso de ubicación ya está otorgado
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                mapViewModel.fetchUserLocation(context, fusedLocationClient)
            }
            else -> {
                // Solicitar el permiso de ubicación si no se ha concedido
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Muestra el mapa de Google
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // Marcador de Mi Universidad
            Marker(
                state = rememberMarkerState(position = universidadLocation),
                title = "Mi Universidad",
                snippet = "Ubicación de la Universidad"
            )

            // Marcador de la ubicación actual del usuario
            userLocation?.let {
                Marker(
                    state = rememberMarkerState(key = "user_loc", position = it),
                    title = "Tu ubicación",
                    snippet = "Aquí es donde te encuentras actualmente."
                )
            }

            // Marcadores dinámicos de los lugares cercanos consultados a Google Places API (Color Azul)
            nearbyPlaces.forEach { place ->
                Marker(
                    state = rememberMarkerState(key = place.id, position = place.latLng),
                    title = place.name,
                    snippet = place.address,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            // Si hay una ubicación seleccionada por búsqueda
            selectedLocation?.let {
                Marker(
                    state = rememberMarkerState(key = "sel_loc", position = it),
                    title = "Lugar Seleccionado"
                )
            }
        }

        // Botón superior flotante para consultar los lugares cercanos usando Google Places API
        Button(
            onClick = { mapViewModel.fetchNearbyPlaces(context) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            enabled = !isLoadingPlaces
        ) {
            if (isLoadingPlaces) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
                Text("Cargando lugares cercanos...")
            } else {
                Text("Consultar Lugares Cercanos (Places API)")
            }
        }
    }
}