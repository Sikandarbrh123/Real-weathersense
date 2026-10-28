package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.WeatherViewModel
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LocationsScreen
import com.example.ui.screens.RadarScreen
import com.example.ui.screens.SkyDetectorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SurfaceDark

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WeatherSenseApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun WeatherSenseApp(viewModel: WeatherViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()

    val navItems = listOf(
        NavigationItem(
            label = "Forecast",
            selectedIcon = Icons.Filled.WbSunny,
            unselectedIcon = Icons.Outlined.WbSunny,
            tag = "nav_tab_forecast"
        ),
        NavigationItem(
            label = "Sky Scan",
            selectedIcon = Icons.Filled.Sensors,
            unselectedIcon = Icons.Outlined.Sensors,
            tag = "nav_tab_sky_detector"
        ),
        NavigationItem(
            label = "Radar",
            selectedIcon = Icons.Filled.Radar,
            unselectedIcon = Icons.Outlined.Radar,
            tag = "nav_tab_radar"
        ),
        NavigationItem(
            label = "Alerts",
            selectedIcon = Icons.Filled.NotificationsActive,
            unselectedIcon = Icons.Outlined.NotificationsActive,
            tag = "nav_tab_alerts"
        ),
        NavigationItem(
            label = "Locations",
            selectedIcon = Icons.Filled.Public,
            unselectedIcon = Icons.Outlined.Public,
            tag = "nav_tab_locations"
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = activeTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.switchTab(index) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0F172A),
                            selectedTextColor = SkyBlueLight,
                            indicatorColor = SkyBlueLight,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(item.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = activeTab, label = "ScreenCrossfade") { tab ->
                when (tab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToSearch = { viewModel.switchTab(4) },
                        onNavigateToAlerts = { viewModel.switchTab(3) }
                    )
                    1 -> SkyDetectorScreen(viewModel = viewModel)
                    2 -> RadarScreen(viewModel = viewModel)
                    3 -> AlertsScreen(viewModel = viewModel)
                    4 -> LocationsScreen(
                        viewModel = viewModel,
                        onLocationSelected = { viewModel.switchTab(0) }
                    )
                }
            }
        }
    }
}

private data class NavigationItem(
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
)
