package com.subdue.thesteamyspoon.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.subdue.thesteamyspoon.ui.screens.CreateInvoiceScreen
import com.subdue.thesteamyspoon.ui.screens.HomeScreen
import com.subdue.thesteamyspoon.ui.screens.InvoiceDetailScreen
import com.subdue.thesteamyspoon.ui.screens.ProductManagementScreen
import com.subdue.thesteamyspoon.ui.screens.SalesSummaryScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateInvoice : Screen("create_invoice")
    object ManageProducts : Screen("manage_products")
    object SalesSummary : Screen("sales_summary")
    object InvoiceDetail : Screen("invoice_detail/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "invoice_detail/$invoiceId"
    }
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
                },
                onNavigateToSalesSummary = {
                    navController.navigate(Screen.SalesSummary.route)
                },
                onNavigateToInvoiceDetail = { invoiceId ->
                    navController.navigate(Screen.InvoiceDetail.createRoute(invoiceId))
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
        
        composable(Screen.SalesSummary.route) {
            SalesSummaryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.InvoiceDetail.route,
            arguments = listOf(
                navArgument("invoiceId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            InvoiceDetailScreen(
                invoiceId = invoiceId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

