plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val appEnv = file(".env").takeIf { it.isFile }?.readLines()
    ?.filter { it.contains("=") && !it.trimStart().startsWith("#") }
    ?.associate { line ->
        val key = line.substringBefore("=").trim()
        val value = line.substringAfter("=").trim().removeSurrounding("\"").removeSurrounding("'")
        key to value
    } ?: emptyMap()

fun String.asBuildConfigLiteral(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.desarrollomovielexample.actividadapirest"
    compileSdk { version = release(37) }
    defaultConfig {
        applicationId = "com.desarrollomovielexample.actividadapirest"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "SUPABASE_URL", (appEnv["SUPABASE_URL"] ?: "").asBuildConfigLiteral())
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            (appEnv["SUPABASE_ANON_KEY"] ?: appEnv["SUPABASE_KEY"] ?: "").asBuildConfigLiteral()
        )
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    ksp(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
