package com.edufast.mobile

import android.app.Application
import com.edufast.mobile.data.ApiEduFastRepository
import com.edufast.mobile.data.TokenStore
import com.edufast.mobile.domain.repository.EduFastRepository

// DI manual (sin frameworks): aquí se arma la "fábrica" de dependencias.
// Cuando la app crece, esto se cambia por Hilt/Dagger.
class EduFastApp : Application() {

    lateinit var repository: EduFastRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val baseUrl = BuildConfig.API_BASE_URL
        check(baseUrl.isNotBlank()) {
            "API_BASE_URL no está configurada. Define -Pedufast.apiBaseUrl para el build release."
        }
        repository = ApiEduFastRepository(TokenStore(this), baseUrl)
    }
}