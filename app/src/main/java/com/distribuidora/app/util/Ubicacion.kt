package com.distribuidora.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

private fun tienePermisoUbicacion(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

@Suppress("MissingPermission")
private fun obtenerUbicacion(context: Context, onResult: (Punto?) -> Unit) {
    val cliente = LocationServices.getFusedLocationProviderClient(context)
    val cts = CancellationTokenSource()
    cliente.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
        .addOnSuccessListener { loc ->
            if (loc != null) onResult(Punto(loc.latitude, loc.longitude))
            else cliente.lastLocation
                .addOnSuccessListener { last ->
                    onResult(last?.let { Punto(it.latitude, it.longitude) })
                }
                .addOnFailureListener { onResult(null) }
        }
        .addOnFailureListener { onResult(null) }
}

/**
 * Devuelve una función que solicita la ubicación actual, pidiendo permiso
 * si hace falta, y entrega el [Punto] (o null) en [onResult].
 */
@Composable
fun rememberUbicacionActual(): ((onResult: (Punto?) -> Unit) -> Unit) {
    val context = LocalContext.current
    val pendiente = remember { arrayOfNulls<(Punto?) -> Unit>(1) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos.values.any { it }
        val cb = pendiente[0]
        if (concedido) {
            obtenerUbicacion(context) { cb?.invoke(it) }
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            cb?.invoke(null)
        }
    }

    return remember {
        { onResult ->
            if (tienePermisoUbicacion(context)) {
                obtenerUbicacion(context, onResult)
            } else {
                pendiente[0] = onResult
                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
}
