/*
 * Copyright 2022. Explore in HMS. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hms.xmedia.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("settings")

@Singleton
class DataStoreManager @Inject constructor(val context: Context) {

    private val settingsDataStore = context.dataStore

    val isOnBoardingShowed: Flow<Boolean>
        get() = settingsDataStore.data.map { preferences ->
            preferences[KEY_ONBOARD] ?: false
        }

    suspend fun saveOnBoardingState(state: Boolean) {
        settingsDataStore.edit { preferences ->
            preferences[KEY_ONBOARD] = state
        }
    }

    companion object {
        val KEY_ONBOARD = booleanPreferencesKey("onboard")
    }


}