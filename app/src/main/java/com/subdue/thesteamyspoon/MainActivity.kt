package com.subdue.thesteamyspoon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.subdue.thesteamyspoon.ui.components.AppNavigationDrawer
import com.subdue.thesteamyspoon.ui.navigation.NavGraph
import com.subdue.thesteamyspoon.ui.navigation.Screen
import com.subdue.thesteamyspoon.ui.theme.TheSteamySpoonTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheSteamySpoonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            AppNavigationDrawer(
                                currentRoute = currentRoute,
                                onNavigateToHome = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = false }
                                    }
                                    scope.launch { drawerState.close() }
                                },
                                onNavigateToManageProducts = {
                                    navController.navigate(Screen.ManageProducts.route)
                                    scope.launch { drawerState.close() }
                                },
                                onDismiss = { scope.launch { drawerState.close() } }
                            )
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { },
                                    navigationIcon = {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Text("☰", fontSize = 24.sp)
                                        }
                                    }
                                )
                            }
                        ) { paddingValues ->
                            NavGraph(
                                navController = navController,
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                    }
                }
            }
        }
    }
}