package com.distribuidora.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY nombre COLLATE NOCASE ASC")
    fun observarTodos(): Flow<List<Cliente>>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun obtener(id: Long): Cliente?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cliente: Cliente): Long

    @Update
    suspend fun actualizar(cliente: Cliente)

    @Delete
    suspend fun eliminar(cliente: Cliente)
}

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos ORDER BY nombre COLLATE NOCASE ASC")
    fun observarTodos(): Flow<List<Producto>>

    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre COLLATE NOCASE ASC")
    fun observarActivos(): Flow<List<Producto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: Producto): Long

    @Update
    suspend fun actualizar(producto: Producto)

    @Delete
    suspend fun eliminar(producto: Producto)
}

@Dao
interface PedidoDao {
    @Transaction
    @Query("SELECT * FROM pedidos ORDER BY (fechaEntrega IS NULL), fechaEntrega ASC, fechaCreacion DESC")
    fun observarTodos(): Flow<List<PedidoConDetalles>>

    @Transaction
    @Query("SELECT * FROM pedidos WHERE estado = :estado ORDER BY (fechaEntrega IS NULL), fechaEntrega ASC, fechaCreacion DESC")
    fun observarPorEstado(estado: EstadoPedido): Flow<List<PedidoConDetalles>>

    @Transaction
    @Query("SELECT * FROM pedidos WHERE id = :id")
    suspend fun obtenerConDetalles(id: Long): PedidoConDetalles?

    @Insert
    suspend fun insertarPedido(pedido: Pedido): Long

    @Update
    suspend fun actualizarPedido(pedido: Pedido)

    @Delete
    suspend fun eliminarPedido(pedido: Pedido)

    @Insert
    suspend fun insertarItems(items: List<PedidoItem>)

    @Query("DELETE FROM pedido_items WHERE pedidoId = :pedidoId")
    suspend fun borrarItemsDe(pedidoId: Long)

    @Query("UPDATE pedidos SET estado = :estado WHERE id = :pedidoId")
    suspend fun cambiarEstado(pedidoId: Long, estado: EstadoPedido)

    /** Inserta o actualiza un pedido junto con sus renglones en una sola transacción. */
    @Transaction
    suspend fun guardarPedidoCompleto(pedido: Pedido, items: List<PedidoItem>): Long {
        val pedidoId: Long
        if (pedido.id == 0L) {
            pedidoId = insertarPedido(pedido)
        } else {
            pedidoId = pedido.id
            actualizarPedido(pedido)
            borrarItemsDe(pedidoId)
        }
        insertarItems(items.map { it.copy(id = 0, pedidoId = pedidoId) })
        return pedidoId
    }
}
