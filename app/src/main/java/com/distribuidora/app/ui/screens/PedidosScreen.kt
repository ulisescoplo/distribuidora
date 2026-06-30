package com.distribuidora.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.distribuidora.app.data.EstadoPedido
import com.distribuidora.app.data.PedidoConDetalles
import com.distribuidora.app.ui.DialogoConfirmar
import com.distribuidora.app.ui.EstadoVacio
import com.distribuidora.app.ui.Factories
import com.distribuidora.app.ui.PedidoViewModel
import com.distribuidora.app.ui.collectAsStateWithLifecycleCompat
import com.distribuidora.app.ui.formatoFecha
import com.distribuidora.app.ui.formatoMoneda
import com.distribuidora.app.util.abrirPunto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosScreen(
    onNuevo: () -> Unit,
    onEditar: (Long) -> Unit,
    vm: PedidoViewModel = viewModel(factory = Factories.pedido)
) {
    var soloPendientes by remember { mutableStateOf(true) }
    val todos by vm.pedidos.collectAsStateWithLifecycleCompat()
    val pendientes by vm.pedidosPendientes.collectAsStateWithLifecycleCompat()
    val lista = if (soloPendientes) pendientes else todos
    val context = LocalContext.current
    var aEliminar by remember { mutableStateOf<PedidoConDetalles?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pedidos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo pedido")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = soloPendientes,
                    onClick = { soloPendientes = true },
                    label = { Text("Pendientes (${pendientes.size})") }
                )
                FilterChip(
                    selected = !soloPendientes,
                    onClick = { soloPendientes = false },
                    label = { Text("Todos (${todos.size})") }
                )
            }

            if (lista.isEmpty()) {
                EstadoVacio(
                    Icons.Filled.ReceiptLong,
                    if (soloPendientes) "No hay pedidos pendientes." else "Aún no hay pedidos.\nToca + para crear uno."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lista, key = { it.pedido.id }) { item ->
                        TarjetaPedido(
                            item = item,
                            onClick = { onEditar(item.pedido.id) },
                            onEntregado = {
                                vm.cambiarEstado(item.pedido.id, EstadoPedido.ENTREGADO)
                            },
                            onMapa = {
                                val c = item.cliente
                                if (c.tieneUbicacion) abrirPunto(context, c.latitud!!, c.longitud!!, c.nombre)
                            },
                            onEliminar = { aEliminar = item }
                        )
                    }
                }
            }
        }
    }

    aEliminar?.let { it0 ->
        DialogoConfirmar(
            titulo = "Eliminar pedido",
            texto = "¿Eliminar el pedido de ${it0.cliente.nombre}?",
            onConfirmar = { vm.eliminar(it0.pedido); aEliminar = null },
            onCancelar = { aEliminar = null }
        )
    }
}

@Composable
private fun TarjetaPedido(
    item: PedidoConDetalles,
    onClick: () -> Unit,
    onEntregado: () -> Unit,
    onMapa: () -> Unit,
    onEliminar: () -> Unit
) {
    val p = item.pedido
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.cliente.nombre,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(p.estado.etiqueta) }
                )
            }
            Text("Entrega: ${formatoFecha(p.fechaEntrega)}", style = MaterialTheme.typography.bodyMedium)
            val resumen = item.items.joinToString(", ") { "${it.cantidad}× ${it.nombreProducto}" }
            if (resumen.isNotBlank())
                Text(resumen, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatoMoneda(p.total),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                if (item.cliente.tieneUbicacion) {
                    IconButton(onClick = onMapa) {
                        Icon(Icons.Filled.Place, contentDescription = "Ver en mapa", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                if (p.estado == EstadoPedido.PENDIENTE) {
                    IconButton(onClick = onEntregado) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Marcar entregado", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}
