package com.example.daytracker.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.daytracker.ui.screens.AboutScreen
import com.example.daytracker.ui.screens.AddEditEventScreen
import com.example.daytracker.ui.screens.EventDetailsScreen
import com.example.daytracker.ui.screens.HomeScreen
import com.example.daytracker.ui.screens.SettingsScreen
import com.example.daytracker.ui.screens.SplashScreen
import com.example.daytracker.ui.theme.DayTrackerTheme

object Screen {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val ADD_EDIT = "add_edit?eventId={eventId}"
    const val DETAILS = "details/{eventId}"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    fun addEdit(eventId: Long = -1L): String = "add_edit?eventId=$eventId"
    fun details(eventId: Long): String = "details/$eventId"
}

@Composable
fun DayTrackerApp(
    modifier: Modifier = Modifier,
    viewModel: DayTrackerViewModel = viewModel(
        factory = DayTrackerViewModel.provideFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    // Observe app resume to refresh day calculations dynamically when date changes
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshCurrentDate()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DayTrackerTheme(themeMode = uiState.themeMode) {
        NavHost(
            navController = navController,
            startDestination = Screen.SPLASH,
            modifier = modifier,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            composable(Screen.SPLASH) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.HOME) {
                            popUpTo(Screen.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.HOME) {
                HomeScreen(
                    uiState = uiState,
                    onSearchChange = viewModel::onSearchQueryChange,
                    onCategorySelect = viewModel::onCategoryFilterChange,
                    onSortSelect = viewModel::onSortOptionChange,
                    onEventClick = { eventId ->
                        viewModel.selectEvent(eventId)
                        navController.navigate(Screen.details(eventId))
                    },
                    onAddClick = {
                        navController.navigate(Screen.addEdit())
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.SETTINGS)
                    },
                    onAboutClick = {
                        navController.navigate(Screen.ABOUT)
                    },
                    onClearMessage = viewModel::clearUserMessage
                )
            }

            composable(
                route = Screen.ADD_EDIT,
                arguments = listOf(
                    navArgument("eventId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: -1L
                val existingEvent = if (eventId != -1L) {
                    uiState.allEvents.firstOrNull { it.entity.id == eventId }?.entity
                } else null

                AddEditEventScreen(
                    existingEvent = existingEvent,
                    dateFormat = uiState.dateFormat,
                    onBackClick = { navController.popBackStack() },
                    onSaveEvent = { id, title, date, desc, cat, icon, color, onSuccess ->
                        viewModel.saveEvent(id, title, date, desc, cat, icon, color, onSuccess)
                    }
                )
            }

            composable(
                route = Screen.DETAILS,
                arguments = listOf(
                    navArgument("eventId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: -1L
                val item = uiState.selectedEvent ?: uiState.allEvents.firstOrNull { it.entity.id == eventId }

                EventDetailsScreen(
                    item = item,
                    userMessage = uiState.userMessage,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { id ->
                        navController.navigate(Screen.addEdit(id))
                    },
                    onDeleteClick = { entity ->
                        viewModel.deleteEvent(entity) {
                            navController.popBackStack()
                        }
                    },
                    onCopyClick = viewModel::copyEventStatistics,
                    onShareClick = viewModel::shareEventDetails,
                    onClearMessage = viewModel::clearUserMessage
                )
            }

            composable(Screen.SETTINGS) {
                SettingsScreen(
                    currentTheme = uiState.themeMode,
                    currentDateFormat = uiState.dateFormat,
                    userMessage = uiState.userMessage,
                    onThemeSelected = viewModel::setThemeMode,
                    onDateFormatSelected = viewModel::setDateFormat,
                    onLoadSampleEvents = viewModel::loadSampleEvents,
                    onClearAllEvents = viewModel::clearAllEvents,
                    onBackClick = { navController.popBackStack() },
                    onClearMessage = viewModel::clearUserMessage
                )
            }

            composable(Screen.ABOUT) {
                AboutScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
