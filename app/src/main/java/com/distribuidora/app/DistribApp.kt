package com.distribuidora.app

import android.app.Application
import com.distribuidora.app.data.AppDatabase
import com.distribuidora.app.data.AppRepository

/** Application: crea la base de datos y el repositorio una sola vez. */
class DistribApp : Application() {
    val repository: AppRepository by lazy {
        val db = AppDatabase.obtener(this)
        AppRepository(db.clienteDao(), db.productoDao(), db.pedidoDao())
    }
}
