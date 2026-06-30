package com.distribuidora.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.WrongLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.distribuidora.app.ui.EstadoVacio
import com.distribuidora.app.ui.Factories
import com.distribuidora.app.ui.PedidoViewModel
import com.distribuidora.app.ui.collectAsStateWithLifecycleCompat
import com.distribuidora.app.ui.formatoFecha
import com.distribuidora.app.util.Punto
import com.distribuidora.app.util.abrirEnMaps
import com.distribuidora.app.util.construirUrlRuta
import com.distribuidora.app.util.ordenarPorCercania
import com.distribuidora.app.util.rememberUbicacionActual

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutaScreen(
    vm: PedidoViewModel = viewModel(factory = Factories.pedido)
) {
    val context = LocalContext.current
    val pendientes by vm.pedidosPendientes.collectAsStateWithLifecycleCompat()
    val conUbicacion = pendientes.filter { it.cliente.tieneUbicacion }

    val seleccion = remember { mutableStateMapOf<Long, Boolean>() }
    var origen by remember { mutableStateOf<Punto?>(null) }
    val pedirUbicacion = rememberUbicacionActual()

    fun estaSeleccionado(id: Long) = seleccion[id] ?: true

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ruta de entrega") }) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (conUbicacion.isEmpty()) {
                EstadoVacio(
                    Icons.Filled.WrongLocation,
                    "No hay pedidos pendientes con ubicación.\nAgregá coordenadas a tus clientes para trazar la ruta."
                )
            } else {
                Text(
                    "Elegí los pedidos a incluir. Google Maps admite hasta 9 paradas por viaje.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                OutlinedButton(
                    onClick = {
                        pedirUbicacion { punto ->
                            origen = punto
                            Toast.makeText(
                                context,
                                if (punto != null) "Ubicación de inicio fijada" else "No se pudo obtener la ubicación",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null)
                    Text(if (origen == null) "  Partir desde mi ubicación actual" else "  Inicio: mi ubicación ✓")
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(conUbicacion, key = { it.pedido.id }) { item ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = estaSeleccionado(item.pedido.id),
                                    onCheckedChange = { seleccion[item.pedido.id] = it }
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(item.cliente.nombre, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Entrega: ${formatoFecha(item.pedido.fechaEntrega)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (item.cliente.direccion.isNotBlank())
                                        Text(
                                            item.cliente.direccion,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val destinos = conUbicacion
                            .filter { estaSeleccionado(it.pedido.id) }
                            .map { Punto(it.cliente.latitud!!, it.cliente.longitud!!, it.cliente.nombre) }
                        if (destinos.isEmpty()) {
                            Toast.makeText(context, "Seleccioná al menos un pedido", Toast.LENGTH_SHORT).show()
                        } else {
                            if (destinos.size > 9) {
                                Toast.makeText(context, "Hay más de 9 paradas; Maps usará las primeras 9.", Toast.LENGTH_LONG).show()
                            }
                            val ordenados = ordenarPorCercania(origen, destinos.take(9))
                            abrirEnMaps(context, construirUrlRuta(origen, ordenados))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null)
                    Text("  Trazar ruta en Google Maps")
                }
            }
        }
    }
}
