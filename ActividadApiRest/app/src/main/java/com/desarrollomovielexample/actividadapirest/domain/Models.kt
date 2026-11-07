package com.desarrollomovielexample.actividadapirest.domain

import com.desarrollomovielexample.actividadapirest.data.remote.dto.FarmaciaMedicamentoDto
import com.desarrollomovielexample.actividadapirest.data.remote.dto.MedicamentoDto

data class Medicamento(
    val id: String,
    val nombreComercial: String,
    val principioActivo: String?,
    val laboratorio: String?,
    val presentacion: String?,
    val concentracion: String?,
    val imagenUrl: String?,
    val inventario: List<Inventario> = emptyList()
)

data class Inventario(
    val stockActual: Int?,
    val precioUnitario: Double?,
    val farmacia: Farmacia?
)

data class Farmacia(
    val id: String,
    val nombre: String,
    val nit: String?,
    val direccion: String?,
    val latitud: Double?,
    val longitud: Double?,
    val activa: Boolean?,
    val imagenUrl: String?,
    val horaApertura: String?,
    val horaCierre: String?
)

data class MedicamentoEnFarmacia(
    val medicamento: Medicamento,
    val stockActual: Int?,
    val precioUnitario: Double?
)

fun MedicamentoDto.toDomain() = Medicamento(
    id = id,
    nombreComercial = nombreComercial,
    principioActivo = principioActivo,
    laboratorio = laboratorio,
    presentacion = presentacion,
    concentracion = concentracion,
    imagenUrl = imagenUrl,
    inventario = inventario.map { row ->
        Inventario(
            stockActual = row.stockActual,
            precioUnitario = row.precioUnitario,
            farmacia = row.farmacias?.let { shop ->
                Farmacia(
                    id = shop.id,
                    nombre = shop.nombreSucursal,
                    nit = shop.nit,
                    direccion = shop.direccion,
                    latitud = shop.latitud,
                    longitud = shop.longitud,
                    activa = shop.activa,
                    imagenUrl = shop.imagenUrl,
                    horaApertura = shop.horaApertura,
                    horaCierre = shop.horaCierre
                )
            }
        )
    }
)

fun FarmaciaMedicamentoDto.toDomain(): MedicamentoEnFarmacia? =
    medicamentos?.let { medicamento ->
        MedicamentoEnFarmacia(
            medicamento = medicamento.toDomain(),
            stockActual = stockActual,
            precioUnitario = precioUnitario
        )
    }
