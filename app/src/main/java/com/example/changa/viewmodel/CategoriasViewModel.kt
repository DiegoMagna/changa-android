package com.example.changa.viewmodel

import androidx.lifecycle.*
import com.example.changa.models.dto.CategoriaResponse
import com.example.changa.models.dto.CategoriasResponse
import com.example.changa.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoriasViewModel : ViewModel() {

    private val _categorias = MutableLiveData<List<CategoriaResponse>>()
    val categorias: LiveData<List<CategoriaResponse>> get() = _categorias

    private val _categoriasSeleccionadas = MutableLiveData<List<CategoriaResponse>>(emptyList())
    val categoriasSeleccionadas: LiveData<List<CategoriaResponse>> get() = _categoriasSeleccionadas

    init {
        obtenerCategoriasDesdeDB()
    }

    private fun obtenerCategoriasDesdeDB() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.obtenerCategorias()
                _categorias.postValue(response) // 👍 ya es una lista

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun agregarCategoria(categoria: CategoriaResponse) {
        val actual = _categoriasSeleccionadas.value ?: emptyList()
        if (!actual.contains(categoria)) {
            _categoriasSeleccionadas.postValue(actual + categoria)
        }
    }

    fun eliminarCategoria(categoria: CategoriaResponse) {
        _categoriasSeleccionadas.postValue(_categoriasSeleccionadas.value?.filter { it != categoria })
    }

    fun obtenerIdsSeleccionados(): List<Int> {
        return _categoriasSeleccionadas.value?.map { it.id } ?: emptyList()
    }
}
