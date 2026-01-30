package com.example.changa.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePrestadorPerfilRequest(
    val zonaTexto: String,
    val radioKm: Int,
    val activo: Boolean,
    val modoGuardia: Boolean,
    val bio: String,
    val fotoUrl: String
)
