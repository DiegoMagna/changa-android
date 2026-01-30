package com.example.changa.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.libraries.places.api.net.PlacesClient

object Routes {
    const val AltaPrestador = "alta_prestador"
    const val EditarPerfil = "editar_perfil?prestadorId={prestadorId}"

    fun editarPerfilRoute(prestadorId: String?): String {
        return if (prestadorId.isNullOrBlank()) {
            "editar_perfil"
        } else {
            "editar_perfil?prestadorId=$prestadorId"
        }
    }
}

@Composable
fun AppNavHost(placesClient: PlacesClient) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.AltaPrestador
    ) {
        composable(Routes.AltaPrestador) {
            AltaPrestadorScreen(
                placesClient = placesClient,
                onPrestadorCreado = { prestadorId ->
                    navController.navigate(Routes.editarPerfilRoute(prestadorId))
                }
            )
        }
        composable(
            route = Routes.EditarPerfil,
            arguments = listOf(
                navArgument("prestadorId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val prestadorId = backStackEntry.arguments?.getString("prestadorId")
            EditarPerfilScreen(
                prestadorId = prestadorId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
