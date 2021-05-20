package com.example.munros.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.munrolibrary.constants.MunroCategories
import com.example.munrolibrary.constants.MunroSortOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore: DataStore<Preferences> = context.dataStore

    object PreferencesKeys {
        val CATEGORY = stringPreferencesKey("category")
        val MIN_HEIGHT = stringPreferencesKey("min_height")
        val MAX_HEIGHT = stringPreferencesKey("max_height")
        val ITEM_LIMIT = stringPreferencesKey("item_limit")
        val HEIGHT_SORT = stringPreferencesKey("height_sort")
        val NAME_SORT = stringPreferencesKey("name_sort")
    }

    suspend fun applyAll(
        @MunroCategories category: String,
        minHeight: String?,
        maxHeight: String?,
        itemLimit: String?,
        @MunroSortOptions heightSort: String,
        @MunroSortOptions nameSort: String
    ) {
        val selectedCategory = when (category) {
            MunroCategories.MUNRO -> MunroCategories.MUNRO
            MunroCategories.MUNRO_TOP -> MunroCategories.MUNRO_TOP
            else -> MunroCategories.NONE
        }
        val selectedHeightSort = when (heightSort) {
            MunroSortOptions.DESC -> MunroSortOptions.DESC
            else -> MunroSortOptions.ASC
        }
        val selectedNameSort = when (nameSort) {
            MunroSortOptions.DESC -> MunroSortOptions.DESC
            else -> MunroSortOptions.ASC
        }

        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CATEGORY] = selectedCategory
            preferences[PreferencesKeys.MIN_HEIGHT] = minHeight ?: ""
            preferences[PreferencesKeys.MAX_HEIGHT] = maxHeight ?: ""
            preferences[PreferencesKeys.ITEM_LIMIT] = itemLimit ?: ""
            preferences[PreferencesKeys.HEIGHT_SORT] = selectedHeightSort
            preferences[PreferencesKeys.NAME_SORT] = selectedNameSort
        }
    }

    val categoryFilterFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is Exception) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferencesKeys.CATEGORY] ?: MunroCategories.NONE
        }

    val minHeightFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is Exception) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferencesKeys.MIN_HEIGHT] ?: ""
        }

    val maxHeightFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is Exception) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferencesKeys.MAX_HEIGHT] ?: ""
        }

    val itemLimitFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is Exception) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferencesKeys.ITEM_LIMIT] ?: ""
        }

    val heightSortFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is Exception) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferencesKeys.HEIGHT_SORT] ?: MunroSortOptions.ASC
        }

    val nameSortFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is Exception) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferencesKeys.NAME_SORT] ?: MunroSortOptions.ASC
        }


}