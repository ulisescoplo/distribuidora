package com.distribuidora.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.distribuidora.app.DistribApp
import com.distribuidora.app.data.AppRepository
import com.distribuidora.app.data.Cliente
import com.distribuidora.app.data.EstadoPedido
import com.distribuidora.app.data.Pedido
import com.distribuidora.app.data.PedidoConDetalles
import com.distribuidora.app.data.PedidoItem
import com.distribuidora.app.data.Producto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private fun <T> kotlinx.coroutines.flow.Flow<T>.asState(vm: ViewModel, inicial: T): StateFlow<T> =
    stateIn(vm.viewModelScope, SharingStarted.WhileSubscribed(5000), inicial)

class ClienteViewModel(private val repo: AppRepository) : ViewModel() {
    val clientes: StateFlow<List<Cliente>> = repo.clientes.asState(this, emptyList())

    fun guardar(cliente: Cliente) = viewModelScope.launch { repo.guardarCliente(cliente) }
    fun eliminar(cliente: Cliente) = viewModelScope.launch { repo.eliminarCliente(cliente) }
}

class ProductoViewModel(private val repo: AppRepository) : ViewModel() {
    val productos: StateFlow<List<Producto>> = repo.productos.asState(this, emptyList())

    fun guardar(producto: Producto) = viewModelScope.launch { repo.guardarProducto(producto) }
    fun eliminar(producto: Producto) = viewModelScope.launch { repo.eliminarProducto(producto) }
}

class PedidoViewModel(private val repo: AppRepository) : ViewModel() {
    val pedidos: StateFlow<List<PedidoConDetalles>> = repo.pedidos.asState(this, emptyList())
    val pedidosPendientes: StateFlow<List<PedidoConDetalles>> =
        repo.pedidosPendientes.asState(this, emptyList())
    val clientes: StateFlow<List<Cliente>> = repo.clientes.asState(this, emptyList())
    val productosActivos: StateFlow<List<Producto>> = repo.productosActivos.asState(this, emptyList())

    fun guardar(pedido: Pedido, items: List<PedidoItem>) =
        viewModelScope.launch { repo.guardarPedido(pedido, items) }

    fun cambiarEstado(pedidoId: Long, estado: EstadoPedido) =
        viewModelScope.launch { repo.cambiarEstadoPedido(pedidoId, estado) }

    fun eliminar(pedido: Pedido) = viewModelScope.launch { repo.eliminarPedido(pedido) }

    fun cargarPedido(id: Long, onLoaded: (PedidoConDetalles?) -> Unit) =
        viewModelScope.launch { onLoaded(repo.obtenerPedido(id)) }
}

/** Helper para construir factories que obtienen el repositorio desde la Application. */
fun <VM : ViewModel> appFactory(crear: (AppRepository) -> VM): ViewModelProvider.Factory =
    viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as DistribApp
            crear(app.repository)
        }
    }

object Factories {
    val cliente = appFactory { ClienteViewModel(it) }
    val producto = appFactory { ProductoViewModel(it) }
    val pedido = appFactory { PedidoViewModel(it) }
}
