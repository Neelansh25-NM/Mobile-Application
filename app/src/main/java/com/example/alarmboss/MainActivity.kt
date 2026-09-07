package com.example.alarmboss

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.alarmboss.ui.alarms.AlarmEditScreen
import com.example.alarmboss.ui.alarms.AlarmListScreen
import com.example.alarmboss.ui.settings.SettingsScreen
import com.example.alarmboss.ui.stopwatch.StopwatchScreen
import com.example.alarmboss.ui.theme.AlarmBossTheme

private object Routes {
    const val ALARMS = "alarms"
    const val EDIT = "edit"
    const val STOPWATCH = "stopwatch"
    const val SETTINGS = "settings"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AlarmBossTheme {
                RequestRuntimePermissions()
                AlarmBossRoot()
            }
        }
    }
}

@Composable
private fun RequestRuntimePermissions() {
    // Notifications (Android 13+) and camera/audio are requested contextually where they're
    // used (task/exercise screens); this just grabs the notification permission up front so
    // the alarm's full-screen notification can post at all.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmBossRoot() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination

                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.ALARMS } == true,
                    onClick = { navController.navigate(Routes.ALARMS) { popUpTo(navController.graph.findStartDestination().id) } },
                    icon = { Icon(Icons.Default.Alarm, contentDescription = "Alarms") },
                    label = { Text("Alarms") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.STOPWATCH } == true,
                    onClick = { navController.navigate(Routes.STOPWATCH) { popUpTo(navController.graph.findStartDestination().id) } },
                    icon = { Icon(Icons.Default.Timer, contentDescription = "Stopwatch") },
                    label = { Text("Stopwatch") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.SETTINGS } == true,
                    onClick = { navController.navigate(Routes.SETTINGS) { popUpTo(navController.graph.findStartDestination().id) } },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.ALARMS,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.ALARMS) {
                AlarmListScreen(
                    onAddAlarm = { navController.navigate("${Routes.EDIT}/new") },
                    onEditAlarm = { id -> navController.navigate("${Routes.EDIT}/$id") }
                )
            }
            composable("${Routes.EDIT}/{alarmId}") { backStackEntry ->
                val idArg = backStackEntry.arguments?.getString("alarmId")
                val alarmId = idArg?.toLongOrNull()
                AlarmEditScreen(alarmId = alarmId, onDone = { navController.popBackStack() })
            }
            composable(Routes.STOPWATCH) { StopwatchScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
        }
    }
}
