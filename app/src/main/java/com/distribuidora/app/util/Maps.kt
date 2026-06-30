package com.distribuidora.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Punto(val lat: Double, val lng: Double, val etiqueta: String = "")

/** Distancia aproximada en km entre dos puntos (fórmula de Haversine). */
fun distanciaKm(a: Punto, b: Punto): Double {
    val r = 6371.0
    val dLat = Math.toRadians(b.lat - a.lat)
    val dLng = Math.toRadians(b.lng - a.lng)
    val s = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) *
        sin(dLng / 2) * sin(dLng / 2)
    return r * 2 * atan2(sqrt(s), sqrt(1 - s))
}

/**
 * Ordena los destinos buscando el "mejor recorrido" con la heurística del
 * vecino más cercano partiendo de [origen] (o del primer destino si es null).
 */
fun ordenarPorCercania(origen: Punto?, destinos: List<Punto>): List<Punto> {
    if (destinos.size <= 1) return destinos
    val pendientes = destinos.toMutableList()
    val ruta = mutableListOf<Punto>()
    var actual = origen ?: pendientes.removeAt(0).also { ruta.add(it) }
    while (pendientes.isNotEmpty()) {
        val siguiente = pendientes.minByOrNull { distanciaKm(actual, it) }!!
        pendientes.remove(siguiente)
        ruta.add(siguiente)
        actual = siguiente
    }
    return ruta
}

/**
 * Construye la URL de Google Maps con la ruta de entrega.
 * Google Maps admite hasta ~9 puntos intermedios.
 */
fun construirUrlRuta(origen: Punto?, destinosOrdenados: List<Punto>): String? {
    if (destinosOrdenados.isEmpty()) return null
    val destino = destinosOrdenados.last()
    val intermedios = destinosOrdenados.dropLast(1)
    val sb = StringBuilder("https://www.google.com/maps/dir/?api=1&travelmode=driving")
    if (origen != null) sb.append("&origin=${origen.lat},${origen.lng}")
    sb.append("&destination=${destino.lat},${destino.lng}")
    if (intermedios.isNotEmpty()) {
        val wp = intermedios.joinToString("|") { "${it.lat},${it.lng}" }
        sb.append("&waypoints=").append(Uri.encode(wp))
    }
    return sb.toString()
}

fun abrirEnMaps(context: Context, url: String?) {
    if (url == null) {
        Toast.makeText(context, "No hay ubicaciones para trazar", Toast.LENGTH_SHORT).show()
        return
    }
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }.onFailure {
        Toast.makeText(context, "No se pudo abrir Google Maps", Toast.LENGTH_SHORT).show()
    }
}

/** Abre la ubicación de un solo cliente en Google Maps. */
fun abrirPunto(context: Context, lat: Double, lng: Double, @Suppress("UNUSED_PARAMETER") etiqueta: String) {
    val url = "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
    abrirEnMaps(context, url)
}
