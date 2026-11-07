package com.desarrollomovielexample.actividadapirest.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FarmaciaMedicamentoDto(
    @SerialName("id_farmacia") val idFarmacia: String,
    @SerialName("stock_actual") val stockActual: Int? = null,
    @SerialName("precio_unitario") val precioUnitario: Double? = null,
    val medicamentos: MedicamentoDto? = null
)
