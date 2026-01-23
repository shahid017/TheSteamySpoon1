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
import com.subdue.thesteamyspoon.ui.screens.SalesAnalyticsScreen
import com.subdue.thesteamyspoon.ui.screens.SalesScreen
import com.subdue.thesteamyspoon.ui.screens.SalesSummaryScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateInvoice : Screen("create_invoice") {
        fun createRoute(invoiceId: Long? = null): String {
            return if (invoiceId != null && invoiceId > 0) {
                "${route}?invoiceId=$invoiceId"
            } else {
                route
            }
        }
    }
    object ManageProducts : Screen("manage_products")
    object SalesSummary : Screen("sales_summary")
    object SalesAnalytics : Screen("sales_analytics")
    object Sales : Screen("sales")
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
                    navController.navigate(Screen.CreateInvoice.createRoute())
                },
                onNavigateToManageProducts = {
                    navController.navigate(Screen.ManageProducts.route)
                },
                onNavigateToSalesSummary = {
                    navController.navigate(Screen.SalesSummary.route)
                },
                onNavigateToSalesAnalytics = {
                    navController.navigate(Screen.SalesAnalytics.route)
                },
                onNavigateToInvoiceDetail = { invoiceId ->
                    navController.navigate(Screen.InvoiceDetail.createRoute(invoiceId))
                }
            )
        }
        
        composable(
            route = "${Screen.CreateInvoice.route}?invoiceId={invoiceId}",
            arguments = listOf(
                navArgument("invoiceId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId")?.takeIf { it > 0 }
            CreateInvoiceScreen(
                invoiceId = invoiceId,
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

        composable(Screen.SalesAnalytics.route) {
            SalesAnalyticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Sales.route) {
            SalesScreen(
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
                },
                onEditInvoice = { id ->
                    navController.navigate(Screen.CreateInvoice.createRoute(id))
                }
            )
        }
    }
}

