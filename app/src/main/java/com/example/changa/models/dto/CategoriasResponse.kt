package com.example.changa.models.dto
import com.example.changa.models.Categoria
import kotlinx.serialization.Serializable

@Serializable
data class CategoriaResponse(
    val id: Int,
    val nombre: String
)

@Serializable
data class CategoriasResponse(
    val categorias: List<CategoriaResponse>
)