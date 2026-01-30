package com.example.changa.models.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrestadorCreateResponse(
    @SerialName("id")
    val id: Long? = null
)
