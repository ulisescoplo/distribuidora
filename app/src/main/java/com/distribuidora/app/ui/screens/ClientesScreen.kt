package com.distribuidora.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.distribuidora.app.data.Cliente
import com.distribuidora.app.ui.CampoTexto
import com.distribuidora.app.ui.ClienteViewModel
import com.distribuidora.app.ui.collectAsStateWithLifecycleCompat
import com.distribuidora.app.ui.DialogoConfirmar
import com.distribuidora.app.ui.EstadoVacio
import com.distribuidora.app.ui.Factories
import com.distribuidora.app.util.abrirPunto
import com.distribuidora.app.util.rememberUbicacionActual

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(
    onNuevo: () -> Unit,
    onEditar: (Long) -> Unit,
    vm: ClienteViewModel = viewModel(factory = Factories.cliente)
) {
    val clientes by vm.clientes.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current
    var aEliminar by remember { mutableStateOf<Cliente?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Clientes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo cliente")
            }
        }
    ) { padding ->
        if (clientes.isEmpty()) {
            Column(Modifier.padding(padding).fillMaxSize()) {
                EstadoVacio(Icons.Filled.PeopleOutline, "Aún no hay clientes.\nToca + para agregar el primero.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(clientes, key = { it.id }) { cliente ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditar(cliente.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(cliente.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                if (cliente.telefono.isNotBlank())
                                    Text(cliente.telefono, style = MaterialTheme.typography.bodyMedium)
                                if (cliente.direccion.isNotBlank())
                                    Text(cliente.direccion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (cliente.tieneUbicacion) {
                                IconButton(onClick = {
                                    abrirPunto(context, cliente.latitud!!, cliente.longitud!!, cliente.nombre)
                                }) {
                                    Icon(Icons.Filled.Place, contentDescription = "Ver en mapa", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            IconButton(onClick = { aEliminar = cliente }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }

    aEliminar?.let { c ->
        DialogoConfirmar(
            titulo = "Eliminar cliente",
            texto = "¿Eliminar a ${c.nombre}? También se borrarán sus pedidos.",
            onConfirmar = { vm.eliminar(c); aEliminar = null },
            onCancelar = { aEliminar = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteEditScreen(
    clienteId: Long,
    onVolver: () -> Unit,
    vm: ClienteViewModel = viewModel(factory = Factories.cliente)
) {
    val clientes by vm.clientes.collectAsStateWithLifecycleCompat()
    val existente = clientes.firstOrNull { it.id == clienteId }

    var nombre by remember(existente) { mutableStateOf(existente?.nombre ?: "") }
    var telefono by remember(existente) { mutableStateOf(existente?.telefono ?: "") }
    var direccion by remember(existente) { mutableStateOf(existente?.direccion ?: "") }
    var lat by remember(existente) { mutableStateOf(existente?.latitud?.toString() ?: "") }
    var lng by remember(existente) { mutableStateOf(existente?.longitud?.toString() ?: "") }
    var notas by remember(existente) { mutableStateOf(existente?.notas ?: "") }

    val pedirUbicacion = rememberUbicacionActual()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (clienteId == 0L) "Nuevo cliente" else "Editar cliente") },
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
                        Cliente(
                            id = clienteId,
                            nombre = nombre.trim(),
                            telefono = telefono.trim(),
                            direccion = direccion.trim(),
                            latitud = lat.toDoubleOrNull(),
                            longitud = lng.toDoubleOrNull(),
                            notas = notas.trim()
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
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoTexto(nombre, { nombre = it }, "Nombre *", Modifier.fillMaxWidth())
            CampoTexto(telefono, { telefono = it }, "Teléfono", Modifier.fillMaxWidth(), KeyboardType.Phone)
            CampoTexto(direccion, { direccion = it }, "Dirección", Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTexto(lat, { lat = it }, "Latitud", Modifier.weight(1f), KeyboardType.Decimal)
                CampoTexto(lng, { lng = it }, "Longitud", Modifier.weight(1f), KeyboardType.Decimal)
            }
            OutlinedButton(
                onClick = {
                    pedirUbicacion { punto ->
                        if (punto != null) {
                            lat = punto.lat.toString()
                            lng = punto.lng.toString()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = null)
                Text("  Usar mi ubicación actual")
            }
            CampoTexto(notas, { notas = it }, "Notas", Modifier.fillMaxWidth(), lineas = 3)
        }
    }
}
