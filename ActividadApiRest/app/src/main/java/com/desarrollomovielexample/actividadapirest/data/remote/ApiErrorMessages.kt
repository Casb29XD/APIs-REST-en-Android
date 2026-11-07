package com.desarrollomovielexample.actividadapirest.data.remote

import java.io.IOException
import retrofit2.HttpException

fun Throwable.toApiMessage(): String = when (this) {
    is HttpException -> {
        val serverMessage = response()?.errorBody()?.string()
            ?.takeIf { it.isNotBlank() }
        val friendlyMessage = when (code()) {
            400 -> "La consulta enviada a Supabase no es válida."
            401 -> "Supabase rechazó la clave de API."
            403 -> "Acceso denegado. Revisa los permisos y las políticas RLS."
            404 -> "No se encontró el endpoint, la tabla o la relación solicitada."
            409 -> "La solicitud entra en conflicto con los datos actuales."
            429 -> "Se alcanzó el límite de solicitudes. Intenta de nuevo más tarde."
            in 500..599 -> "Supabase tuvo un error interno. Intenta de nuevo más tarde."
            else -> "Supabase respondió con HTTP ${code()}."
        }
        if (serverMessage == null) friendlyMessage else "$friendlyMessage Detalle: $serverMessage"
    }
    is IOException -> "No se pudo conectar. Comprueba tu conexión e inténtalo de nuevo."
    else -> localizedMessage ?: "Ocurrió un error inesperado."
}
