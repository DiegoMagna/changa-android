package com.example.changa.models

import kotlinx.serialization.Serializable

@Serializable
data class PrestadorRequest(
    val nombre: String,
    val email: String,
    val telefono: String,
    val direccion: String,
    val direccionCoordenadas: String,
    val dni: Int,                // 👈 Nuevo campo
    val cuit: Long,              // 👈 Nuevo campo
    val categoriaIds: List<Int>
)
