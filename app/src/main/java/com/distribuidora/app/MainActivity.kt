package com.distribuidora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.distribuidora.app.ui.screens.ClienteEditScreen
import com.distribuidora.app.ui.screens.ClientesScreen
import com.distribuidora.app.ui.screens.PedidoEditScreen
import com.distribuidora.app.ui.screens.PedidosScreen
import com.distribuidora.app.ui.screens.ProductoEditScreen
import com.distribuidora.app.ui.screens.ProductosScreen
import com.distribuidora.app.ui.screens.RutaScreen
import com.distribuidora.app.ui.theme.DistribuidoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DistribuidoraTheme {
                Surface { AppPrincipal() }
            }
        }
    }
}

private data class ItemNav(val ruta: String, val titulo: String, val icono: ImageVector)

private val itemsNav = listOf(
    ItemNav("pedidos", "Pedidos", Icons.Filled.ShoppingCart),
    ItemNav("ruta", "Ruta", Icons.Filled.Route),
    ItemNav("clientes", "Clientes", Icons.Filled.People),
    ItemNav("productos", "Productos", Icons.Filled.Inventory2)
)

@Composable
fun AppPrincipal() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val rutaActual = backStack?.destination?.route
    val mostrarBarra = itemsNav.any { it.ruta == rutaActual }

    Scaffold(
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar {
                    val destinoActual = backStack?.destination
                    itemsNav.forEach { item ->
                        NavigationBarItem(
                            selected = destinoActual?.hierarchy?.any { it.route == item.ruta } == true,
                            onClick = {
                                navController.navigate(item.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icono, contentDescription = item.titulo) },
                            label = { Text(item.titulo) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "pedidos",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("pedidos") {
                PedidosScreen(
                    onNuevo = { navController.navigate("pedido_edit/0") },
                    onEditar = { id -> navController.navigate("pedido_edit/$id") }
                )
            }
            composable("pedido_edit/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                PedidoEditScreen(pedidoId = id, onVolver = { navController.popBackStack() })
            }
            composable("ruta") { RutaScreen() }
            composable("clientes") {
                ClientesScreen(
                    onNuevo = { navController.navigate("cliente_edit/0") },
                    onEditar = { id -> navController.navigate("cliente_edit/$id") }
                )
            }
            composable("cliente_edit/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                ClienteEditScreen(clienteId = id, onVolver = { navController.popBackStack() })
            }
            composable("productos") {
                ProductosScreen(
                    onNuevo = { navController.navigate("producto_edit/0") },
                    onEditar = { id -> navController.navigate("producto_edit/$id") }
                )
            }
            composable("producto_edit/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                ProductoEditScreen(productoId = id, onVolver = { navController.popBackStack() })
            }
        }
    }
}
