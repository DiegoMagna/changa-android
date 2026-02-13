package com.example.changa.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.snapshotFlow

import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.changa.models.PrestadorRequest
import com.example.changa.models.dto.CategoriaResponse
import com.example.changa.network.RetrofitClient
import com.example.changa.viewmodel.CategoriasViewModel
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.google.android.gms.common.api.ApiException




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AltaPrestadorScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoriasViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    placesClient: PlacesClient,
    onPrestadorCreado: (String?) -> Unit = {}
) {
    val placesLogTag = "PLACES_AUTOCOMPLETE"
    val token = remember { AutocompleteSessionToken.newInstance() }

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var cuit by remember { mutableStateOf("") }
    var direccionesSugeridas by remember { mutableStateOf(listOf<String>()) }
    var direccionCoordenadas by remember { mutableStateOf("") }
    val categorias by viewModel.categorias.observeAsState(emptyList())
    val categoriasSeleccionadas by viewModel.categoriasSeleccionadas.observeAsState(emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    var categoriaSeleccionada by remember { mutableStateOf<CategoriaResponse?>(null) }

    var expanded by remember { mutableStateOf(false) }
    var suggestionsExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        snapshotFlow { direccion.trim() }
            .map { it.replace(Regex("\\s+"), " ") }
            .distinctUntilChanged()
            .debounce(350)
            .collectLatest { query ->
                if (query.length >= 3) {
                    Log.d(placesLogTag, "Requesting autocomplete for query=\"$query\"")
                    val sugerencias = buscarSugerenciasDireccion(query, placesClient, token)
                    direccionesSugeridas = sugerencias
                    suggestionsExpanded = sugerencias.isNotEmpty()
                } else {
                    direccionesSugeridas = emptyList()
                    suggestionsExpanded = false
                }
            }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Alta de Prestador", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(telefono, { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(dni, { dni = it }, label = { Text("DNI") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(cuit, { cuit = it }, label = { Text("CUIT") }, modifier = Modifier.fillMaxWidth())

            ExposedDropdownMenuBox(
                expanded = suggestionsExpanded,
                onExpandedChange = { /* controlled by query results */ }
            ) {
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = suggestionsExpanded,
                    onDismissRequest = { suggestionsExpanded = false }
                ) {
                    direccionesSugeridas.forEach { sugerencia ->
                        DropdownMenuItem(
                            text = { Text(sugerencia) },
                            onClick = {
                                direccion = sugerencia
                                direccionesSugeridas = emptyList()
                                suggestionsExpanded = false
                                coroutineScope.launch {
                                    direccionCoordenadas = obtenerCoordenadasDesdeDireccion(sugerencia)
                                }
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = categoriaSeleccionada?.nombre ?: "Seleccionar especialidad",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.nombre) },
                            onClick = {
                                categoriaSeleccionada = categoria
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = { categoriaSeleccionada?.let { viewModel.agregarCategoria(it) } },
                modifier = Modifier.fillMaxWidth(),
                enabled = categoriaSeleccionada != null
            ) {
                Text("Agregar Categoría")
            }

            Text("Categorías Seleccionadas:", style = MaterialTheme.typography.bodyLarge)

            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                items(categoriasSeleccionadas) { categoria ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(categoria.nombre)
                        IconButton(onClick = { viewModel.eliminarCategoria(categoria) }) {
                            Icon(Icons.Default.Close, contentDescription = "Eliminar")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!email.contains("@") || !email.contains(".")) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("❌ Email inválido")
                        }
                        return@Button
                    }

                    val dniInt = dni.toIntOrNull()
                    val cuitLong = cuit.toLongOrNull()

                    if (dniInt == null || cuitLong == null) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("❌ DNI o CUIT inválidos")
                        }
                        return@Button
                    }

                    guardarPrestador(
                        nombre,
                        email,
                        telefono,
                        direccion,
                        direccionCoordenadas,
                        dniInt,
                        cuitLong,
                        viewModel.obtenerIdsSeleccionados(),
                        snackbarHostState,
                        onPrestadorCreado
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Prestador")
            }
        }
    }
}

suspend fun buscarSugerenciasDireccion(
    input: String,
    placesClient: PlacesClient,
    token: AutocompleteSessionToken
): List<String> = suspendCancellableCoroutine { continuation ->

    val request = FindAutocompletePredictionsRequest.builder()
        .setSessionToken(token)
        .setQuery(input)
        // Opcional pero recomendado:
        // .setCountries(listOf("AR"))
        .build()

    Log.d("PLACES_AUTOCOMPLETE", "Requesting Places Autocomplete for \"$input\"")

    val task = placesClient.findAutocompletePredictions(request)
        .addOnSuccessListener { response ->
            val sugerencias = response.autocompletePredictions
                .map { it.getFullText(null).toString() }
            Log.d("PLACES_AUTOCOMPLETE", "OK (${sugerencias.size}) -> $sugerencias")
            if (continuation.isActive) {
                continuation.resume(sugerencias)
            }
        }
        .addOnFailureListener { e ->
            val msg = when (e) {
                is ApiException -> "ApiException status=${e.statusCode} message=${e.message}"
                else -> e.message ?: e.toString()
            }
            Log.e("PLACES_AUTOCOMPLETE", "Autocomplete FAIL -> $msg", e)
            if (continuation.isActive) {
                continuation.resume(emptyList())
            }
        }

    continuation.invokeOnCancellation {
        // no-op (no se puede cancelar el Task)
    }





}

suspend fun obtenerCoordenadasDesdeDireccion(direccion: String): String {
    delay(500)
    return "-32.9468,-60.6393"
}

fun guardarPrestador(
    nombre: String,
    email: String,
    telefono: String,
    direccion: String,
    direccionCoordenadas: String,
    dni: Int,
    cuit: Long,
    categoriaIds: List<Int>,
    snackbarHostState: SnackbarHostState,
    onPrestadorCreado: (String?) -> Unit
) {
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        CoroutineScope(Dispatchers.Main).launch {
            snackbarHostState.showSnackbar("❌ Email inválido")
        }
        return
    }

    if (dni.toString().length != 8) {
        CoroutineScope(Dispatchers.Main).launch {
            snackbarHostState.showSnackbar("❌ DNI debe tener 8 dígitos")
        }
        return
    }

    if (cuit.toString().length != 11) {
        CoroutineScope(Dispatchers.Main).launch {
            snackbarHostState.showSnackbar("❌ CUIT debe tener 11 dígitos")
        }
        return
    }
    val prestador = PrestadorRequest(
        nombre = nombre,
        email = email,
        telefono = telefono,
        direccion = direccion,
        direccionCoordenadas = direccionCoordenadas,
        dni = dni,
        cuit = cuit,
        categoriaIds = categoriaIds
    )

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitClient.apiService.crearPrestador(prestador)
            val errorText = response.errorBody()?.string()
            Log.d("AltaPrestador", "HTTP ${response.code()} ${response.message()}")
            Log.e("AltaPrestador", "ErrorBody: $errorText")

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val prestadorId = response.body()?.id?.toString()
                        ?: response.headers()["Location"]?.substringAfterLast("/")
                    if (prestadorId == null) {
                        Log.w(
                            "AltaPrestador",
                            "Prestador creado sin ID en respuesta. TODO: confirmar API devuelve id."
                        )
                        snackbarHostState.showSnackbar(
                            "✅ Prestador guardado. No recibimos ID; completá el perfil manualmente."
                        )
                    } else {
                        snackbarHostState.showSnackbar("✅ Prestador guardado correctamente")
                    }
                    onPrestadorCreado(prestadorId)
                } else {
                    snackbarHostState.showSnackbar("❌ Error: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Log.e("AltaPrestador", "Error: ${e.message}")
            withContext(Dispatchers.Main) {
                snackbarHostState.showSnackbar("❌ Error al guardar: ${e.message}")
            }
        }
    }
}
