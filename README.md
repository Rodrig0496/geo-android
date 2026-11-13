# locationAndroid

Aplicación Android desarrollada con Jetpack Compose que muestra un mapa de Google Maps con la ubicación actual del usuario.

## Características

- Visualización de Google Maps en pantalla completa
- Obtención de la ubicación actual del dispositivo mediante el proveedor de ubicación de Google Play Services
- Marcador interactivo en la ubicación del usuario con título y descripción
- Solicitud de permisos de ubicación en tiempo de ejecución
- Preparación para búsqueda de lugares por nombre (geocoding con `Geocoder`)
- Tema Material 3 con soporte de color dinámico (Android 12+)

## Tecnologías

| Tecnología | Versión |
|---|---|
| Kotlin | 2.3.21 |
| Android Gradle Plugin | 9.1.1 |
| Jetpack Compose BOM | 2026.05.00 |
| Material 3 | via BOM |
| Google Maps Compose | 8.3.0 |
| Play Services Location | 21.3.0 |
| Google Places API | 5.2.0 |
| Timber | 5.0.1 |
| compileSdk / targetSdk / minSdk | 36 / 36 / 30 |

## Arquitectura

- **Patrón**: Single-activity con ViewModel
- **UI**: Compose con Material 3
- **Estado**: `State` y `mutableStateOf` en ViewModel
- **Concurrencia**: `viewModelScope` con `Dispatchers.IO` para geocoding
- **Sin DI**: No se utiliza Hilt, Koin ni Dagger
- **Sin navegación**: Pantalla única

## Estructura del proyecto

```
app/
├── src/main/
│   ├── java/com/smi/locatioandroid/
│   │   ├── MainActivity.kt
│   │   ├── screens/
│   │   │   └── MapScreen.kt
│   │   ├── viewmodel/
│   │   │   └── MapViewModel.kt
│   │   ├── utils/
│   │   │   └── ManifestUtils.kt
│   │   └── ui/theme/
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   ├── AndroidManifest.xml
│   └── res/
├── build.gradle.kts
```

## Configuración

1. Clona el repositorio.
2. Abre el proyecto en Android Studio.
3. La API key de Google Maps está configurada en `AndroidManifest.xml`.
4. Compila y ejecuta en un dispositivo físico o emulador con Google Play Services.
5. Concede el permiso de ubicación cuando la app lo solicite.

## Requisitos

- Android 11+ (API 30)
- Google Play Services
- Conexión a internet
- Permiso de ubicación (`ACCESS_FINE_LOCATION`)
