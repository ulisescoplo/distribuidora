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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.distribuidora.app.data.Producto
import com.distribuidora.app.ui.CampoTexto
import com.distribuidora.app.ui.DialogoConfirmar
import com.distribuidora.app.ui.EstadoVacio
import com.distribuidora.app.ui.Factories
import com.distribuidora.app.ui.ProductoViewModel
import com.distribuidora.app.ui.collectAsStateWithLifecycleCompat
import com.distribuidora.app.ui.formatoMoneda

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    onNuevo: () -> Unit,
    onEditar: (Long) -> Unit,
    vm: ProductoViewModel = viewModel(factory = Factories.producto)
) {
    val productos by vm.productos.collectAsStateWithLifecycleCompat()
    var aEliminar by remember { mutableStateOf<Producto?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Productos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo producto")
            }
        }
    ) { padding ->
        if (productos.isEmpty()) {
            Column(Modifier.padding(padding).fillMaxSize()) {
                EstadoVacio(Icons.Filled.Inventory2, "Aún no hay productos.\nToca + para agregar uno.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productos, key = { it.id }) { producto ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onEditar(producto.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    producto.nombre + if (!producto.activo) "  (inactivo)" else "",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "${formatoMoneda(producto.precio)} / ${producto.unidad}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (producto.descripcion.isNotBlank())
                                    Text(producto.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { aEliminar = producto }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }

    aEliminar?.let { p ->
        DialogoConfirmar(
            titulo = "Eliminar producto",
            texto = "¿Eliminar ${p.nombre}?",
            onConfirmar = { vm.eliminar(p); aEliminar = null },
            onCancelar = { aEliminar = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoEditScreen(
    productoId: Long,
    onVolver: () -> Unit,
    vm: ProductoViewModel = viewModel(factory = Factories.producto)
) {
    val productos by vm.productos.collectAsStateWithLifecycleCompat()
    val existente = productos.firstOrNull { it.id == productoId }

    var nombre by remember(existente) { mutableStateOf(existente?.nombre ?: "") }
    var descripcion by remember(existente) { mutableStateOf(existente?.descripcion ?: "") }
    var precio by remember(existente) { mutableStateOf(existente?.precio?.toString() ?: "") }
    var unidad by remember(existente) { mutableStateOf(existente?.unidad ?: "unidad") }
    var activo by remember(existente) { mutableStateOf(existente?.activo ?: true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productoId == 0L) "Nuevo producto" else "Editar producto") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (nombre.isNotBlank()) {
                FloatingActionButton(onClick = {
                    vm.guardar(
                        Producto(
                            id = productoId,
                            nombre = nombre.trim(),
                            descripcion = descripcion.trim(),
                            precio = precio.replace(",", ".").toDoubleOrNull() ?: 0.0,
                            unidad = unidad.trim().ifBlank { "unidad" },
                            activo = activo
                        )
                    )
                    onVolver()
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Guardar")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoTexto(nombre, { nombre = it }, "Nombre *", Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTexto(precio, { precio = it }, "Precio", Modifier.weight(1f), KeyboardType.Decimal)
                CampoTexto(unidad, { unidad = it }, "Unidad", Modifier.weight(1f))
            }
            CampoTexto(descripcion, { descripcion = it }, "Descripción", Modifier.fillMaxWidth(), lineas = 3)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = activo, onCheckedChange = { activo = it })
                Text("  Producto activo (disponible para pedidos)")
            }
        }
    }
}
