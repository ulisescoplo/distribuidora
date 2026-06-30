package com.distribuidora.app.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** Estado de un pedido. */
enum class EstadoPedido(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado")
}

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val telefono: String = "",
    val direccion: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val notas: String = ""
) {
    val tieneUbicacion: Boolean get() = latitud != null && longitud != null
}

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val descripcion: String = "",
    val precio: Double = 0.0,
    val unidad: String = "unidad",
    val activo: Boolean = true
)

@Entity(
    tableName = "pedidos",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clienteId")]
)
data class Pedido(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clienteId: Long,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaEntrega: Long? = null,
    val estado: EstadoPedido = EstadoPedido.PENDIENTE,
    val notas: String = "",
    val total: Double = 0.0
)

@Entity(
    tableName = "pedido_items",
    foreignKeys = [
        ForeignKey(
            entity = Pedido::class,
            parentColumns = ["id"],
            childColumns = ["pedidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pedidoId"), Index("productoId")]
)
data class PedidoItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pedidoId: Long,
    val productoId: Long,
    val nombreProducto: String,
    val cantidad: Int,
    val precioUnitario: Double
) {
    val subtotal: Double get() = cantidad * precioUnitario
}

/** Pedido con su cliente y los renglones de productos. */
data class PedidoConDetalles(
    @Embedded val pedido: Pedido,
    @Relation(parentColumn = "clienteId", entityColumn = "id")
    val cliente: Cliente,
    @Relation(parentColumn = "id", entityColumn = "pedidoId")
    val items: List<PedidoItem>
)
