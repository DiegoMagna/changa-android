package com.example.changa.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
import com.example.changa.models.PrestadorRequest
import com.example.changa.models.dto.CategoriaResponse // ✅ FALTABA ESTE IMPORT

interface ApiService {
    @GET("categorias")
    suspend fun obtenerCategorias(): List<CategoriaResponse>



    @POST("prestadores")
    suspend fun crearPrestador(@Body prestador: PrestadorRequest): Response<Unit>
}
