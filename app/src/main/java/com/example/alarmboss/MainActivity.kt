package com.example.alarmboss

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alarmboss.ui.alarms.AlarmEditScreen
import com.example.alarmboss.ui.alarms.AlarmListScreen
import com.example.alarmboss.ui.settings.SettingsScreen
import com.example.alarmboss.ui.stats.StatsScreen
import com.example.alarmboss.ui.stats.StatsViewModel
import com.example.alarmboss.ui.stopwatch.StopwatchScreen
import com.example.alarmboss.ui.theme.AlarmBossTheme
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Spa
import com.example.alarmboss.ui.counselor.SleepCounselorScreen
import com.example.alarmboss.ui.parent.ParentModeScreen

private object Routes {
    const val MAIN = "main"
    const val EDIT = "edit"
    const val PARENT_MODE = "parent_mode"
}

/** The five top-level screens, in the order they sit in the pager and the bottom bar. */
private enum class Tab(val labelRes: Int, val icon: ImageVector) {
    ALARMS(R.string.nav_alarms, Icons.Default.Alarm),
    STATS(R.string.nav_stats, Icons.Default.EmojiEvents),
    COUNSELOR(R.string.nav_counselor, Icons.Default.Spa),
    STOPWATCH(R.string.nav_stopwatch, Icons.Default.Timer),
    SETTINGS(R.string.nav_settings, Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AlarmBossTheme {
                RequestRuntimePermissions()
                AlarmBossAppWithLaunchScreen()
            }
        }
    }
}

@Composable
private fun AlarmBossAppWithLaunchScreen() {
    var isLaunching by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1200L) // smooth 1.2s branded launch screen
        isLaunching = false
    }

    androidx.compose.animation.Crossfade(
        targetState = isLaunching,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 400),
        label = "LaunchCrossfade"
    ) { launching ->
        if (launching) {
            LaunchScreen()
        } else {
            AlarmBossRoot()
        }
    }
}

@Composable
private fun LaunchScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        androidx.compose.ui.graphics.Color(0xFF0F081D),
                        androidx.compose.ui.graphics.Color(0xFF1B0A2E),
                        androidx.compose.ui.graphics.Color(0xFF09040E)
                    )
                )
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
                color = androidx.compose.ui.graphics.Color(0xFF1E0E38),
                border = androidx.compose.foundation.BorderStroke(2.5.dp, androidx.compose.ui.graphics.Color(0xFF9D4EDD)),
                shadowElevation = 16.dp,
                modifier = Modifier.size(160.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_alarm_boss_logo),
                    contentDescription = "AlarmBoss Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                color = androidx.compose.ui.graphics.Color.White
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Conquer Your Mornings",
                style = MaterialTheme.typography.bodyLarge,
                color = androidx.compose.ui.graphics.Color(0xFFC77DFF),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(
                color = androidx.compose.ui.graphics.Color(0xFFE0AAFF),
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )
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

@Composable
private fun AlarmBossRoot() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainTabs(
                onAddAlarm = { navController.navigate("${Routes.EDIT}/new") },
                onEditAlarm = { id -> navController.navigate("${Routes.EDIT}/$id") },
                onOpenParentMode = { navController.navigate(Routes.PARENT_MODE) }
            )
        }
        composable("${Routes.EDIT}/{alarmId}") { backStackEntry ->
            val idArg = backStackEntry.arguments?.getString("alarmId")
            val alarmId = idArg?.toLongOrNull()
            AlarmEditScreen(alarmId = alarmId, onDone = { navController.popBackStack() })
        }
        composable(Routes.PARENT_MODE) {
            ParentModeScreen(onBack = { navController.popBackStack() })
        }
    }
}

/**
 * The five top-level screens as swipeable pages. The bottom bar and the pager share one
 * [androidx.compose.foundation.pager.PagerState], so tapping a tab and swiping stay in sync, and
 * every page stays composed, so each keeps its scroll position and state while you move around.
 */
@Composable
private fun MainTabs(
    onAddAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onOpenParentMode: () -> Unit
) {
    val tabs = Tab.entries
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()

    // Back from any other tab returns to Alarms before leaving the app.
    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = pagerState.currentPage == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                        label = { 
                            Text(
                                stringResource(tab.labelRes),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(padding),
            beyondViewportPageCount = tabs.size - 1
        ) { page ->
            when (tabs[page]) {
                Tab.ALARMS -> AlarmListScreen(
                    onAddAlarm = onAddAlarm,
                    onEditAlarm = onEditAlarm,
                    onOpenParentMode = onOpenParentMode
                )
                Tab.STATS -> {
                    val app = LocalContext.current.applicationContext as AlarmBossApp
                    val statsViewModel: StatsViewModel = viewModel(
                        factory = viewModelFactory { initializer { StatsViewModel(app.profileRepository) } }
                    )
                    StatsScreen(statsViewModel)
                }
                Tab.COUNSELOR -> SleepCounselorScreen()
                Tab.STOPWATCH -> StopwatchScreen()
                Tab.SETTINGS -> SettingsScreen(onOpenParentMode = onOpenParentMode)
            }
        }
    }
}
