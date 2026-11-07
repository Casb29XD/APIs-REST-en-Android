package com.desarrollomovielexample.actividadapirest.data.remote.api

import com.desarrollomovielexample.actividadapirest.data.remote.dto.FarmaciaMedicamentoDto
import com.desarrollomovielexample.actividadapirest.data.remote.dto.MedicamentoDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SupabaseApiService {
    @GET("rest/v1/medicamentos")
    suspend fun getMedicamentos(
        @Query("select") select: String = "*",
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("order") order: String = "nombre_comercial.asc"
    ): Response<List<MedicamentoDto>>

    @GET("rest/v1/medicamentos")
    suspend fun searchMedicamentos(
        @Query("select") select: String = "*",
        @Query("or") or: String,
        @Query("laboratorio") laboratorio: String?,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("order") order: String = "nombre_comercial.asc"
    ): Response<List<MedicamentoDto>>

    @GET("rest/v1/medicamentos")
    suspend fun filterByLaboratorio(
        @Query("select") select: String = "*",
        @Query("laboratorio") laboratorio: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("order") order: String = "nombre_comercial.asc"
    ): Response<List<MedicamentoDto>>

    @GET("rest/v1/medicamentos")
    suspend fun getMedicamentoDetalle(
        @Query("id") id: String,
        @Query("select") select: String = "*,inventario(*,farmacias(*))"
    ): Response<List<MedicamentoDto>>

    @GET("rest/v1/inventario")
    suspend fun getMedicamentosPorFarmacia(
        @Query("id_farmacia") idFarmacia: String,
        @Query("select") select: String =
            "id_farmacia,stock_actual,precio_unitario,medicamentos(id,nombre_comercial,principio_activo,concentracion,presentacion,laboratorio,imagen_url)"
    ): Response<List<FarmaciaMedicamentoDto>>
}
