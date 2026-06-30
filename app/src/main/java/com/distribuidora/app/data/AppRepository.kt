package com.distribuidora.app.data

import kotlinx.coroutines.flow.Flow

/** Punto único de acceso a los datos para la capa de UI. */
class AppRepository(
    private val clienteDao: ClienteDao,
    private val productoDao: ProductoDao,
    private val pedidoDao: PedidoDao
) {
    // Clientes
    val clientes: Flow<List<Cliente>> = clienteDao.observarTodos()
    suspend fun obtenerCliente(id: Long) = clienteDao.obtener(id)
    suspend fun guardarCliente(cliente: Cliente): Long =
        if (cliente.id == 0L) clienteDao.insertar(cliente)
        else { clienteDao.actualizar(cliente); cliente.id }
    suspend fun eliminarCliente(cliente: Cliente) = clienteDao.eliminar(cliente)

    // Productos
    val productos: Flow<List<Producto>> = productoDao.observarTodos()
    val productosActivos: Flow<List<Producto>> = productoDao.observarActivos()
    suspend fun guardarProducto(producto: Producto): Long =
        if (producto.id == 0L) productoDao.insertar(producto)
        else { productoDao.actualizar(producto); producto.id }
    suspend fun eliminarProducto(producto: Producto) = productoDao.eliminar(producto)

    // Pedidos
    val pedidos: Flow<List<PedidoConDetalles>> = pedidoDao.observarTodos()
    val pedidosPendientes: Flow<List<PedidoConDetalles>> =
        pedidoDao.observarPorEstado(EstadoPedido.PENDIENTE)
    suspend fun obtenerPedido(id: Long) = pedidoDao.obtenerConDetalles(id)
    suspend fun guardarPedido(pedido: Pedido, items: List<PedidoItem>): Long =
        pedidoDao.guardarPedidoCompleto(pedido, items)
    suspend fun cambiarEstadoPedido(pedidoId: Long, estado: EstadoPedido) =
        pedidoDao.cambiarEstado(pedidoId, estado)
    suspend fun eliminarPedido(pedido: Pedido) = pedidoDao.eliminarPedido(pedido)
}
