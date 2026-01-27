package com.example.changa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.util.Log
import com.example.changa.ui.AltaPrestadorScreen
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

class MainActivity : ComponentActivity() {

    private lateinit var placesClient: PlacesClient
    private val placesLogTag = "PLACES_INIT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Requisitos Google Cloud:
         * - Habilitar la API "Places API" en el proyecto de Google Cloud.
         * - Configurar la API Key con restricción de aplicación Android
         *   (package name + SHA-1) y, opcionalmente, restringir por API.
         * - Billing habilitado en el proyecto.
         */
        val apiKey = getString(R.string.google_maps_key)
        if (apiKey.isBlank()) {
            Log.e(placesLogTag, "API key vacía. Revisar google_maps_key en strings.xml")
        } else {
            Log.d(placesLogTag, "Inicializando Places SDK (apiKey length=${apiKey.length})")
        }

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
            Log.d(placesLogTag, "Places SDK inicializado")
        } else {
            Log.d(placesLogTag, "Places SDK ya estaba inicializado")
        }

        placesClient = Places.createClient(this)
        Log.d(placesLogTag, "PlacesClient creado")

        setContent {
            AltaPrestadorScreen(placesClient = placesClient)
        }
    }
}
