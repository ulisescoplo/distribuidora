package com.distribuidora.app.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val sdfFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es"))
private val sdfFechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es"))

fun formatoFecha(millis: Long?): String =
    if (millis == null) "Sin fecha" else sdfFecha.format(Date(millis))

fun formatoFechaHora(millis: Long): String = sdfFechaHora.format(Date(millis))

fun formatoMoneda(valor: Double): String =
    "$" + String.format(Locale("es"), "%,.2f", valor)
