package com.desarrollomovielexample.actividadapirest.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MedicamentoDto(
    val id: String,
    @SerialName("nombre_comercial") val nombreComercial: String = "",
    @SerialName("principio_activo") val principioActivo: String? = null,
    val concentracion: String? = null,
    val presentacion: String? = null,
    val laboratorio: String? = null,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    val inventario: List<InventarioDto> = emptyList()
)

@Serializable
data class InventarioDto(
    @SerialName("id_farmacia") val idFarmacia: String? = null,
    @SerialName("id_medicamento") val idMedicamento: String? = null,
    @SerialName("stock_actual") val stockActual: Int? = null,
    @SerialName("precio_unitario") val precioUnitario: Double? = null,
    val farmacias: FarmaciaDto? = null
)

@Serializable
data class FarmaciaDto(
    val id: String,
    @SerialName("nombre_sucursal") val nombreSucursal: String = "Farmacia",
    val nit: String? = null,
    val direccion: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val activa: Boolean? = null,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    @SerialName("hora_apertura") val horaApertura: String? = null,
    @SerialName("hora_cierre") val horaCierre: String? = null
)
