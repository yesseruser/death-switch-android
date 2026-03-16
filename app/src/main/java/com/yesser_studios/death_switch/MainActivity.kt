package com.yesser_studios.death_switch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yesser_studios.death_switch.ui.theme.DeathSwitchTheme
import com.yesser_studios.death_switch.ui.theme.Typography
import kotlinx.coroutines.launch

enum class AppDestination(
    val label: String,
    val iconResId: Int
) {
    DeathSwitch(
        label = "Death Switch",
        iconResId = R.drawable.death_switch_icon
    ),
    Statistics(
        label = "Statistics",
        iconResId = R.drawable.stats_icon
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent()
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val screenWidthDp = LocalWindowInfo.current.containerDpSize.width

    val jsonStorage = remember { JsonDeathStorage(context) }
    val repository = remember(jsonStorage) { StatsRepository(jsonStorage) }
    val migrationManager = remember { MigrationManager(context) }

    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            jsonStorage.load()
            migrationManager.migrateIfNeeded(jsonStorage)
        } catch (e: Exception) {
            // Handle error - keep default state
        } finally {
            isInitialized = true
        }
    }

    val totalDeaths by repository.database.collectAsState(initial = DeathsDatabase())
    var selectedDestination by remember { mutableStateOf(AppDestination.DeathSwitch) }

    val useNavigationRail = screenWidthDp >= 600.dp

    DeathSwitchTheme {
        if (useNavigationRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail {
                    AppDestination.entries.forEach { destination ->
                        val selected = selectedDestination == destination
                        NavigationRailItem(
                            selected = selected,
                            onClick = { selectedDestination = destination },
                            label = {
                                Text(
                                    text = destination.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(destination.iconResId),
                                    contentDescription = destination.label
                                )
                            }
                        )
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = selectedDestination,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "screen_transition"
                    ) { destination ->
                        when (destination) {
                            AppDestination.DeathSwitch -> DeathSwitchScreen(
                                repository = repository,
                                totalDeaths = totalDeaths.totalDeaths
                            )
                            AppDestination.Statistics -> StatisticsScreen(
                                repository = repository
                            )
                        }
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(WindowInsets.statusBars.asPaddingValues()),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AnimatedContent(
                            targetState = selectedDestination,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transition"
                        ) { destination ->
                            when (destination) {
                                AppDestination.DeathSwitch -> DeathSwitchScreen(
                                    repository = repository,
                                    totalDeaths = totalDeaths.totalDeaths
                                )
                                AppDestination.Statistics -> StatisticsScreen(
                                    repository = repository
                                )
                            }
                        }
                    }
                    NavigationBar {
                        AppDestination.entries.forEach { destination ->
                            val selected = selectedDestination == destination
                            NavigationBarItem(
                                selected = selected,
                                onClick = { selectedDestination = destination },
                                label = {
                                    Text(
                                        text = destination.label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(destination.iconResId),
                                        contentDescription = destination.label
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeathSwitchScreen(
    repository: StatsRepository,
    totalDeaths: Int
) {
    val scope = rememberCoroutineScope()

    var checked by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    "Death",
                    modifier = Modifier.padding(4.dp),
                    style = Typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = checked,
                    modifier = Modifier.padding(4.dp),
                    onCheckedChange = {
                        checked = it
                        if (checked) {
                            scope.launch {
                                repository.incrementDeath()
                            }
                        }
                    },
                    thumbContent = {
                        if (checked) {
                            Icon(
                                painter = painterResource(R.drawable.check_24px),
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
            if (checked) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "You died!",
                        modifier = Modifier.padding(4.dp),
                        style = Typography.bodyLarge
                    )
                    Button(
                        modifier = Modifier.padding(4.dp),
                        onClick = {
                            checked = false
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.refresh_24px),
                            contentDescription = "Respawn",
                            modifier = Modifier.padding(4.dp)
                        )
                        Text(
                            text = "Respawn",
                            style = Typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        DeathCounter(
            deaths = totalDeaths,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun DeathCounter(
    deaths: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Number of deaths:",
                fontSize = 20.sp,
                modifier = Modifier.padding(4.dp)
            )
            HighlightText(text = "$deaths", modifier = Modifier.padding(4.dp))
        }
    }
}

@Composable
fun HighlightText(text: String, modifier: Modifier = Modifier) {
    Button(onClick = { }, modifier = modifier) {
        Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
