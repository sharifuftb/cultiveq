package com.example.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "cultiveq_session")

class SessionManager(private val context: Context) {

    private val keyCurrentUserId = longPreferencesKey("current_user_id")

    val currentUserIdFlow: Flow<Long?> = context.sessionDataStore.data.map { prefs ->
        prefs[keyCurrentUserId]
    }

    suspend fun setLoggedInUser(userId: Long) {
        context.sessionDataStore.edit { prefs ->
            prefs[keyCurrentUserId] = userId
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(keyCurrentUserId)
        }
    }
}
