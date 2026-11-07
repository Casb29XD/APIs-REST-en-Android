package com.desarrollomovielexample.actividadapirest.di
import com.desarrollomovielexample.actividadapirest.BuildConfig
import com.desarrollomovielexample.actividadapirest.data.remote.api.SupabaseApiService
import com.desarrollomovielexample.actividadapirest.data.repository.MedicamentosRepository
import com.desarrollomovielexample.actividadapirest.data.repository.MedicamentosRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class) object NetworkModule {
    @Provides @Singleton fun client(): OkHttpClient = OkHttpClient.Builder().addInterceptor { chain ->
        val key = BuildConfig.SUPABASE_ANON_KEY
        val requestBuilder = chain.request().newBuilder().header("apikey", key)
        if (key.startsWith("sb_publishable_")) {
            // Publishable keys are API keys, not JWTs; PostgREST must use the anon role.
            requestBuilder.removeHeader("Authorization")
        } else {
            requestBuilder.header("Authorization", "Bearer $key")
        }
        val request: Request = requestBuilder.build()
        chain.proceed(request)
    }.build()
    @Provides @Singleton fun retrofit(client: OkHttpClient): Retrofit {
        val base = BuildConfig.SUPABASE_URL.trimEnd('/') + "/"
        require(base.startsWith("https://")) { "Configura SUPABASE_URL en gradle.properties" }
        return Retrofit.Builder().baseUrl(base).client(client)
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())).build()
    }
    @Provides @Singleton fun api(retrofit: Retrofit): SupabaseApiService = retrofit.create(SupabaseApiService::class.java)
}
@Module @InstallIn(SingletonComponent::class) abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindRepository(impl: MedicamentosRepositoryImpl): MedicamentosRepository
}