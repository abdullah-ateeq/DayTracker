package com.example.daytracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.format.DateTimeFormatter

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

enum class ThemeMode(val title: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class AppDateFormat(val pattern: String, val label: String) {
    MEDIUM("MMM dd, yyyy", "Oct 30, 2026"),
    SLASH_DMY("dd/MM/yyyy", "30/10/2026"),
    SLASH_MDY("MM/dd/yyyy", "10/30/2026"),
    ISO_YMD("yyyy-MM-dd", "2026-10-30");

    val formatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern(pattern)
}

enum class SortOption(val title: String) {
    NEWEST_DATE("Newest Date First"),
    OLDEST_DATE("Oldest Date First"),
    NAME_ASC("Name (A–Z)"),
    MOST_DAYS("Most Days Passed"),
    NEXT_ANNIVERSARY("Closest Anniversary")
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dateFormat: AppDateFormat = AppDateFormat.MEDIUM,
    val sortOption: SortOption = SortOption.NEWEST_DATE
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val SORT_OPTION = stringPreferencesKey("sort_option")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val dateFormatStr = preferences[PreferencesKeys.DATE_FORMAT] ?: AppDateFormat.MEDIUM.name
        val sortOptionStr = preferences[PreferencesKeys.SORT_OPTION] ?: SortOption.NEWEST_DATE.name

        UserPreferences(
            themeMode = try { ThemeMode.valueOf(themeModeStr) } catch (_: Exception) { ThemeMode.SYSTEM },
            dateFormat = try { AppDateFormat.valueOf(dateFormatStr) } catch (_: Exception) { AppDateFormat.MEDIUM },
            sortOption = try { SortOption.valueOf(sortOptionStr) } catch (_: Exception) { SortOption.NEWEST_DATE }
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDateFormat(format: AppDateFormat) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DATE_FORMAT] = format.name
        }
    }

    suspend fun setSortOption(option: SortOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_OPTION] = option.name
        }
    }
}
