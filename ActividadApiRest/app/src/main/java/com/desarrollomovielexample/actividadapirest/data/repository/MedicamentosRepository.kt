package com.desarrollomovielexample.actividadapirest.data.repository

import com.desarrollomovielexample.actividadapirest.data.remote.api.SupabaseApiService
import com.desarrollomovielexample.actividadapirest.domain.Medicamento
import com.desarrollomovielexample.actividadapirest.domain.MedicamentoEnFarmacia
import com.desarrollomovielexample.actividadapirest.domain.toDomain
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response

interface MedicamentosRepository {
    suspend fun list(offset: Int, limit: Int, query: String, laboratory: String): List<Medicamento>
    suspend fun detail(id: String): Medicamento?
    suspend fun medicamentosPorFarmacia(idFarmacia: String): List<MedicamentoEnFarmacia>
}

@Singleton
class MedicamentosRepositoryImpl @Inject constructor(
    private val api: SupabaseApiService
) : MedicamentosRepository {
    private val cache = linkedMapOf<String, Medicamento>()

    override suspend fun list(offset: Int, limit: Int, query: String, laboratory: String): List<Medicamento> {
        return try {
            val labFilter = laboratory.trim().takeIf { it.isNotEmpty() }?.let { "ilike.*$it*" }
            val response = when {
                query.isNotBlank() -> api.searchMedicamentos(
                    or = "(nombre_comercial.ilike.*$query*,principio_activo.ilike.*$query*)",
                    laboratorio = labFilter,
                    limit = limit,
                    offset = offset
                )
                labFilter != null -> api.filterByLaboratorio(
                    laboratorio = labFilter,
                    limit = limit,
                    offset = offset
                )
                else -> api.getMedicamentos(limit = limit, offset = offset)
            }
            response.requireRows().map { dto -> dto.toDomain().also { cache[it.id] = it } }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (offline: IOException) {
            val cached = cache.values.asSequence()
                .filter {
                    query.isBlank() ||
                        it.nombreComercial.contains(query, ignoreCase = true) ||
                        it.principioActivo?.contains(query, ignoreCase = true) == true
                }
                .filter { laboratory.isBlank() || it.laboratorio?.contains(laboratory, ignoreCase = true) == true }
                .drop(offset)
                .take(limit)
                .toList()
            if (cached.isNotEmpty()) cached else throw offline
        }
    }

    override suspend fun detail(id: String): Medicamento? {
        return try {
            api.getMedicamentoDetalle(id = "eq.$id").requireRows()
                .firstOrNull()
                ?.toDomain()
                ?.also { cache[id] = it }
        } catch (offline: IOException) {
            cache[id]
        }
    }

    override suspend fun medicamentosPorFarmacia(idFarmacia: String): List<MedicamentoEnFarmacia> =
        api.getMedicamentosPorFarmacia(idFarmacia = "eq.$idFarmacia")
            .requireRows()
            .mapNotNull { it.toDomain() }
}

private fun <T> Response<List<T>>.requireRows(): List<T> {
    if (!isSuccessful) throw HttpException(this)
    return body().orEmpty()
}
