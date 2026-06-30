package com.distribuidora.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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
import android.widget.Toast
import com.distribuidora.app.data.Cliente
import com.distribuidora.app.data.EstadoPedido
import com.distribuidora.app.data.Pedido
import com.distribuidora.app.data.PedidoItem
import com.distribuidora.app.data.Producto
import com.distribuidora.app.ui.CampoTexto
import com.distribuidora.app.ui.Factories
import com.distribuidora.app.ui.PedidoViewModel
import com.distribuidora.app.ui.collectAsStateWithLifecycleCompat
import com.distribuidora.app.ui.formatoFecha
import com.distribuidora.app.ui.formatoMoneda

private data class LineaEdit(
    val productoId: Long,
    val nombre: String,
    val precio: Double,
    val cantidad: Int
) {
    val subtotal: Double get() = precio * cantidad
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoEditScreen(
    pedidoId: Long,
    onVolver: () -> Unit,
    vm: PedidoViewModel = viewModel(factory = Factories.pedido)
) {
    val context = LocalContext.current
    val clientes by vm.clientes.collectAsStateWithLifecycleCompat()
    val productos by vm.productosActivos.collectAsStateWithLifecycleCompat()

    var clienteSel by remember { mutableStateOf<Cliente?>(null) }
    var fechaEntrega by remember { mutableStateOf<Long?>(null) }
    var estado by remember { mutableStateOf(EstadoPedido.PENDIENTE) }
    var notas by remember { mutableStateOf("") }
    var fechaCreacion by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val lineas = remember { mutableStateListOf<LineaEdit>() }
    var cargado by remember { mutableStateOf(pedidoId == 0L) }

    // Cargar pedido existente una sola vez.
    LaunchedEffect(pedidoId, clientes.isNotEmpty()) {
        if (pedidoId != 0L && !cargado) {
            vm.cargarPedido(pedidoId) { detalle ->
                if (detalle != null) {
                    clienteSel = detalle.cliente
                    fechaEntrega = detalle.pedido.fechaEntrega
                    estado = detalle.pedido.estado
                    notas = detalle.pedido.notas
                    fechaCreacion = detalle.pedido.fechaCreacion
                    lineas.clear()
                    lineas.addAll(detalle.items.map {
                        LineaEdit(it.productoId, it.nombreProducto, it.precioUnitario, it.cantidad)
                    })
                    cargado = true
                }
            }
        }
    }

    var mostrarFecha by remember { mutableStateOf(false) }
    val total = lineas.sumOf { it.subtotal }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (pedidoId == 0L) "Nuevo pedido" else "Editar pedido") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val cli = clienteSel
                when {
                    cli == null -> Toast.makeText(context, "Elegí un cliente", Toast.LENGTH_SHORT).show()
                    lineas.isEmpty() -> Toast.makeText(context, "Agregá al menos un producto", Toast.LENGTH_SHORT).show()
                    else -> {
                        val pedido = Pedido(
                            id = pedidoId,
                            clienteId = cli.id,
                            fechaCreacion = fechaCreacion,
                            fechaEntrega = fechaEntrega,
                            estado = estado,
                            notas = notas.trim(),
                            total = total
                        )
                        val items = lineas.map {
                            PedidoItem(
                                pedidoId = pedidoId,
                                productoId = it.productoId,
                                nombreProducto = it.nombre,
                                cantidad = it.cantidad,
                                precioUnitario = it.precio
                            )
                        }
                        vm.guardar(pedido, items)
                        onVolver()
                    }
                }
            }) {
                Icon(Icons.Filled.Check, contentDescription = "Guardar pedido")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SelectorCliente(clientes, clienteSel) { clienteSel = it }
            }
            item {
                OutlinedButton(onClick = { mostrarFecha = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                    Text("  Fecha de entrega: ${formatoFecha(fechaEntrega)}")
                }
            }
            if (pedidoId != 0L) {
                item { SelectorEstado(estado) { estado = it } }
            }
            item {
                Text("Productos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            item {
                AgregarLinea(productos) { linea -> lineas.add(linea) }
            }
            itemsIndexed(lineas) { idx, linea ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${linea.cantidad}× ${linea.nombre}", fontWeight = FontWeight.Medium)
                            Text(
                                "${formatoMoneda(linea.precio)} c/u  =  ${formatoMoneda(linea.subtotal)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { lineas.removeAt(idx) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                        }
                    }
                }
            }
            item {
                HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("TOTAL", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(
                        formatoMoneda(total),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            item {
                CampoTexto(notas, { notas = it }, "Notas", Modifier.fillMaxWidth(), lineas = 2)
            }
        }
    }

    if (mostrarFecha) {
        val estadoFecha = rememberDatePickerState(initialSelectedDateMillis = fechaEntrega)
        DatePickerDialog(
            onDismissRequest = { mostrarFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaEntrega = estadoFecha.selectedDateMillis
                    mostrarFecha = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarFecha = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = estadoFecha) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorCliente(
    clientes: List<Cliente>,
    seleccionado: Cliente?,
    onSeleccion: (Cliente) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
        OutlinedTextField(
            value = seleccionado?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Cliente *") },
            placeholder = { Text("Seleccionar cliente") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            if (clientes.isEmpty()) {
                DropdownMenuItem(text = { Text("No hay clientes cargados") }, onClick = { expandido = false })
            }
            clientes.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.nombre) },
                    onClick = { onSeleccion(c); expandido = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorEstado(estado: EstadoPedido, onSeleccion: (EstadoPedido) -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
        OutlinedTextField(
            value = estado.etiqueta,
            onValueChange = {},
            readOnly = true,
            label = { Text("Estado") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            EstadoPedido.values().forEach { e ->
                DropdownMenuItem(
                    text = { Text(e.etiqueta) },
                    onClick = { onSeleccion(e); expandido = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgregarLinea(
    productos: List<Producto>,
    onAgregar: (LineaEdit) -> Unit
) {
    var productoSel by remember { mutableStateOf<Producto?>(null) }
    var cantidad by remember { mutableStateOf("1") }
    var expandido by remember { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
                OutlinedTextField(
                    value = productoSel?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Producto") },
                    placeholder = { Text("Seleccionar producto") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                    if (productos.isEmpty()) {
                        DropdownMenuItem(text = { Text("No hay productos activos") }, onClick = { expandido = false })
                    }
                    productos.forEach { p ->
                        DropdownMenuItem(
                            text = { Text("${p.nombre} — ${formatoMoneda(p.precio)}") },
                            onClick = { productoSel = p; expandido = false }
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = {
                        val p = productoSel
                        val c = cantidad.toIntOrNull() ?: 0
                        if (p != null && c > 0) {
                            onAgregar(LineaEdit(p.id, p.nombre, p.precio, c))
                            productoSel = null
                            cantidad = "1"
                        }
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(" Agregar")
                }
            }
        }
    }
}
