package com.subdue.thesteamyspoon.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppNavigationDrawer(
    currentRoute: String?,
    onNavigateToHome: () -> Unit,
    onNavigateToManageProducts: () -> Unit,
    onNavigateToSales: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "The Steamy Spoon",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            NavigationDrawerItem(
                label = { Text("Home") },
                selected = currentRoute == "home",
                onClick = {
                    onNavigateToHome()
                    onDismiss()
                },
                icon = { Text("🏠") }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            NavigationDrawerItem(
                label = { Text("Manage Products") },
                selected = currentRoute == "manage_products",
                onClick = {
                    onNavigateToManageProducts()
                    onDismiss()
                },
                icon = { Text("📦") }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            NavigationDrawerItem(
                label = { Text("Sales") },
                selected = currentRoute == "sales",
                onClick = {
                    onNavigateToSales()
                    onDismiss()
                },
                icon = { Text("📊") }
            )
        }
    }
}

