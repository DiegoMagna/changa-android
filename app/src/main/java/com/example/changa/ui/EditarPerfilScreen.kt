package com.example.changa.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.changa.models.dto.UpdatePrestadorPerfilRequest
import com.example.changa.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    prestadorId: String?,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var zonaTexto by remember { mutableStateOf("") }
    var radioKm by remember { mutableFloatStateOf(10f) }
    var activo by remember { mutableStateOf(true) }
    var modoGuardia by remember { mutableStateOf(false) }
    var bio by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(prestadorId) {
        if (prestadorId.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                "⚠️ No tenemos ID del prestador. Guardado deshabilitado."
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Editar Perfil", style = MaterialTheme.typography.headlineSmall)

            if (prestadorId.isNullOrBlank()) {
                Text(
                    "No se recibió ID del prestador. Volvé atrás o reintenta más tarde.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedTextField(
                value = zonaTexto,
                onValueChange = { zonaTexto = it },
                label = { Text("Zona de trabajo") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Radio de trabajo: ${radioKm.toInt()} km")
            Slider(
                value = radioKm,
                onValueChange = { radioKm = it },
                valueRange = 1f..50f,
                steps = 48
            )

            RowSwitch(
                title = "Activo",
                checked = activo,
                onCheckedChange = { activo = it }
            )

            RowSwitch(
                title = "Modo guardia",
                checked = modoGuardia,
                onCheckedChange = { modoGuardia = it }
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = fotoUrl,
                onValueChange = { fotoUrl = it },
                label = { Text("Foto URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val id = prestadorId?.toLongOrNull()
                    if (id == null) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                "⚠️ No se pudo obtener ID del prestador."
                            )
                        }
                        return@Button
                    }
                    val request = UpdatePrestadorPerfilRequest(
                        zonaTexto = zonaTexto,
                        radioKm = radioKm.toInt(),
                        activo = activo,
                        modoGuardia = modoGuardia,
                        bio = bio,
                        fotoUrl = fotoUrl
                    )
                    isSaving = true
                    Log.d("EditarPerfil", "Actualizando perfil para prestadorId=$id")
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val response =
                                RetrofitClient.apiService.actualizarPerfilPrestador(id, request)
                            withContext(Dispatchers.Main) {
                                if (response.isSuccessful) {
                                    snackbarHostState.showSnackbar("✅ Perfil actualizado")
                                } else {
                                    snackbarHostState.showSnackbar(
                                        "❌ Error al guardar: ${response.code()}"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("EditarPerfil", "Error actualizando perfil: ${e.message}")
                            withContext(Dispatchers.Main) {
                                snackbarHostState.showSnackbar(
                                    "❌ Error al guardar: ${e.message}"
                                )
                            }
                        } finally {
                            withContext(Dispatchers.Main) {
                                isSaving = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving && !prestadorId.isNullOrBlank()
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar")
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RowSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
