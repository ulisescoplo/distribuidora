package com.distribuidora.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun estadoToString(estado: EstadoPedido): String = estado.name

    @TypeConverter
    fun stringToEstado(valor: String): EstadoPedido =
        runCatching { EstadoPedido.valueOf(valor) }.getOrDefault(EstadoPedido.PENDIENTE)
}

@Database(
    entities = [Cliente::class, Producto::class, Pedido::class, PedidoItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
    abstract fun productoDao(): ProductoDao
    abstract fun pedidoDao(): PedidoDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "distribuidora.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCIA = it }
            }
    }
}
