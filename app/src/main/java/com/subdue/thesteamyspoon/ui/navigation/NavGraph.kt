package com.subdue.thesteamyspoon.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.subdue.thesteamyspoon.ui.screens.CreateInvoiceScreen
import com.subdue.thesteamyspoon.ui.screens.HomeScreen
import com.subdue.thesteamyspoon.ui.screens.ProductManagementScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateInvoice : Screen("create_invoice")
    object ManageProducts : Screen("manage_products")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCreateInvoice = {
                    navController.navigate(Screen.CreateInvoice.route)
                },
                onNavigateToManageProducts = {
                    navController.navigate(Screen.ManageProducts.route)
                }
            )
        }
        
        composable(Screen.CreateInvoice.route) {
            CreateInvoiceScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onInvoiceGenerated = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ManageProducts.route) {
            ProductManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

