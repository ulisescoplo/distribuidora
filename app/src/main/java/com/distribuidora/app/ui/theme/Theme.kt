package com.distribuidora.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Azul = Color(0xFF0288D1)
private val AzulOscuro = Color(0xFF01579B)
private val Celeste = Color(0xFF4FC3F7)

private val EsquemaClaro = lightColorScheme(
    primary = Azul,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB3E5FC),
    onPrimaryContainer = AzulOscuro,
    secondary = Color(0xFF00838F),
    onSecondary = Color.White,
    background = Color(0xFFF5FBFF),
    surface = Color.White
)

private val EsquemaOscuro = darkColorScheme(
    primary = Celeste,
    onPrimary = Color(0xFF00344D),
    primaryContainer = AzulOscuro,
    onPrimaryContainer = Color(0xFFB3E5FC),
    secondary = Color(0xFF4DD0E1)
)

@Composable
fun DistribuidoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography(),
        content = content
    )
}
