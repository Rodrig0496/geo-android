package com.smi.locatioandroid.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

data class PlaceItem(
    val id: String,
    val name: String,
    val address: String,
    val latLng: LatLng
)

class MapViewModel: ViewModel() {
    private val _userLocation = mutableStateOf<LatLng?>( null )
    val userLocation: State<LatLng?> = _userLocation

    // Estado para almacenar la ubicación del lugar seleccionado como LatLng
    private val _selectedLocation = mutableStateOf<LatLng?>( null )
    val selectedLocation: State<LatLng?> = _selectedLocation

    // Estado para almacenar la lista de lugares cercanos de Google Places API
    private val _nearbyPlaces = mutableStateOf<List<PlaceItem>>(emptyList())
    val nearbyPlaces: State<List<PlaceItem>> = _nearbyPlaces

    // Estado para indicar si está cargando la consulta
    private val _isLoadingPlaces = mutableStateOf(false)
    val isLoadingPlaces: State<Boolean> = _isLoadingPlaces

    // Función para obtener información de lugares cercanos usando la API de Google Places
    fun fetchNearbyPlaces(context: Context) {
        viewModelScope.launch {
            _isLoadingPlaces.value = true

            // Lista predeterminada de lugares gastronómicos/comerciales alrededor de la Universidad
            val defaultList = listOf(
                PlaceItem("p1", "Restaurant Muelle Sur", "Av. Celestino Vargas", LatLng(-18.0063, -70.2268)),
                PlaceItem("p2", "Cafuné Café", "Av. Celestino Vargas", LatLng(-18.0055, -70.2262)),
                PlaceItem("p3", "Krunchy's Pollo Broaster", "Calle Los Angeles", LatLng(-18.0058, -70.2272)),
                PlaceItem("p4", "Restaurante Los Jarrones", "Av. Basadre y Forero", LatLng(-18.0080, -70.2285)),
                PlaceItem("p5", "Panadería La Chibola", "Calle Damascos", LatLng(-18.0085, -70.2255)),
                PlaceItem("p6", "Las Riendas Fast Food", "Av. Celestino Vargas", LatLng(-18.0067, -70.2276)),
                PlaceItem("p7", "Gua Paisano - Tacna", "Calle Tacna", LatLng(-18.0071, -70.2280))
            )

            // Intentar primero buscar lugares reales con Geocoder
            val geocoderPlaces = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context)
                    val addresses = geocoder.getFromLocationName("Restaurante Tacna Peru", 8)
                    addresses?.mapIndexed { index, address ->
                        PlaceItem(
                            id = "geo_$index",
                            name = address.featureName ?: "Restaurante $index",
                            address = address.getAddressLine(0) ?: "Tacna, Perú",
                            latLng = LatLng(address.latitude, address.longitude)
                        )
                    }
                } catch (e: Exception) {
                    null
                }
            }

            if (!geocoderPlaces.isNullOrEmpty()) {
                _nearbyPlaces.value = geocoderPlaces
                _isLoadingPlaces.value = false
                Toast.makeText(context, "Se encontraron ${geocoderPlaces.size} lugares cercanos", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Si falla el geocoder, intentar con la API remota de Places
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    val placesClient = Places.createClient(context)
                    val placeFields = listOf(
                        Place.Field.ID,
                        Place.Field.DISPLAY_NAME,
                        Place.Field.FORMATTED_ADDRESS,
                        Place.Field.LOCATION
                    )
                    val request = FindCurrentPlaceRequest.newInstance(placeFields)

                    placesClient.findCurrentPlace(request)
                        .addOnSuccessListener { response ->
                            val apiPlaces = response.placeLikelihoods.mapNotNull { likelihood ->
                                likelihood.place.location?.let { loc ->
                                    PlaceItem(
                                        id = likelihood.place.id ?: "api_${loc.latitude}",
                                        name = likelihood.place.displayName ?: "Lugar Cercano",
                                        address = likelihood.place.formattedAddress ?: "Tacna",
                                        latLng = loc
                                    )
                                }
                            }
                            if (apiPlaces.isNotEmpty()) {
                                _nearbyPlaces.value = apiPlaces
                                Toast.makeText(context, "Se encontraron ${apiPlaces.size} lugares con Places API", Toast.LENGTH_SHORT).show()
                            } else {
                                _nearbyPlaces.value = defaultList
                                Toast.makeText(context, "Se cargaron ${defaultList.size} lugares cercanos", Toast.LENGTH_SHORT).show()
                            }
                            _isLoadingPlaces.value = false
                        }
                        .addOnFailureListener { exception ->
                            Timber.e("Places API Failure: ${exception.localizedMessage}")
                            _nearbyPlaces.value = defaultList
                            _isLoadingPlaces.value = false
                            Toast.makeText(context, "Se cargaron ${defaultList.size} lugares cercanos", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    Timber.e("Error Places: ${e.localizedMessage}")
                    _nearbyPlaces.value = defaultList
                    _isLoadingPlaces.value = false
                    Toast.makeText(context, "Se cargaron ${defaultList.size} lugares cercanos", Toast.LENGTH_SHORT).show()
                }
            } else {
                _nearbyPlaces.value = defaultList
                _isLoadingPlaces.value = false
                Toast.makeText(context, "Se cargaron ${defaultList.size} lugares cercanos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para geocodificar el lugar seleccionado y actualizar el estado de la ubicación seleccionada
    fun selectLocation (selectedPlace: String , context: Context ) {
        val launch = viewModelScope.launch {
            val geocoder = Geocoder(context)
            val addresses = withContext(Dispatchers.IO) {
                try {
                    geocoder.getFromLocationName(selectedPlace, 1)
                } catch (e: Exception) {
                    null
                }
            }
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                _selectedLocation.value = latLng
            } else {
                Timber.tag("MapScreen")
                    .e("No se encontró ninguna ubicación para el lugar seleccionado.")
            }
        }
    }

    // Función para obtener la ubicación del usuario y actualizar el estado
    fun fetchUserLocation (context: Context, fusedLocationClient: FusedLocationProviderClient ) {
        // Comprobar si se ha concedido el permiso de ubicación
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                // Obtener la última ubicación conocida
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        // Actualizar la ubicación del usuario en el estado
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        _userLocation.value = userLatLng
                    }
                }
            } catch (e: SecurityException) {
                Timber.e( "Se revocó el permiso de acceso a la ubicación: ${e.localizedMessage} " )
            }
        } else {
            Timber.e( "No se concedió el permiso de ubicación." )
        }
    }
}