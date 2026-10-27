package com.allinedu.mobile

import android.app.Application
import com.allinedu.mobile.data.ApiAllinEduRepository
import com.allinedu.mobile.data.TokenStore
import com.allinedu.mobile.domain.repository.AllinEduRepository

// DI manual (sin frameworks): aquí se arma la "fábrica" de dependencias.
// Cuando la app crece, esto se cambia por Hilt/Dagger.
class AllinEduApp : Application() {

    lateinit var repository: AllinEduRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val baseUrl = BuildConfig.API_BASE_URL
        check(baseUrl.isNotBlank()) {
            "API_BASE_URL no está configurada. Define -Pallinedu.apiBaseUrl para el build release."
        }
        repository = ApiAllinEduRepository(TokenStore(this), baseUrl)
    }
}