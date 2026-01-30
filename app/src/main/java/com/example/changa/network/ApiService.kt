package com.example.changa.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.PATCH
import retrofit2.http.Path
import com.example.changa.models.PrestadorRequest
import com.example.changa.models.dto.CategoriaResponse // ✅ FALTABA ESTE IMPORT
import com.example.changa.models.dto.PrestadorCreateResponse
import com.example.changa.models.dto.UpdatePrestadorPerfilRequest

interface ApiService {
    @GET("categorias")
    suspend fun obtenerCategorias(): List<CategoriaResponse>

    @POST("prestadores")
    suspend fun crearPrestador(@Body prestador: PrestadorRequest): Response<PrestadorCreateResponse>

    @PATCH("prestadores/{id}/perfil")
    suspend fun actualizarPerfilPrestador(
        @Path("id") id: Long,
        @Body request: UpdatePrestadorPerfilRequest
    ): Response<Unit>
}
