package com.nexxlabs.chhotu.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nexxlabs.chhotu.data.local.CommandHistoryRepository.Companion.MAX_HISTORY_SIZE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing command history persistence.
 * Serializes the history list as JSON into DataStore via [AppDataStore].
 */
@Singleton
class CommandHistoryRepository @Inject constructor(
    private val appDataStore: AppDataStore,
    private val gson: Gson
) {
    companion object {
        private const val MAX_HISTORY_SIZE = 20
    }

    private val listType = object : TypeToken<List<CommandHistoryItem>>() {}.type

    /**
     * Emits the current command history, sorted by most recent first.
     */
    val history: Flow<List<CommandHistoryItem>> =
        appDataStore
            .getStringFlow(AppDataStore.COMMAND_HISTORY, "[]")
            .map { json -> deserialize(json) }

    /**
     * Add a new item to the history. Keeps at most [MAX_HISTORY_SIZE] items.
     */
    suspend fun addItem(item: CommandHistoryItem) {
        val current = history.first()
        val updated = (listOf(item) + current).take(MAX_HISTORY_SIZE)
        appDataStore.putString(AppDataStore.COMMAND_HISTORY, serialize(updated))
    }

    /**
     * Clear all command history.
     */
    suspend fun clear() {
        appDataStore.remove(AppDataStore.COMMAND_HISTORY)
    }

    private fun serialize(items: List<CommandHistoryItem>): String {
        return gson.toJson(items)
    }

    private fun deserialize(json: String): List<CommandHistoryItem> {
        return try {
            gson.fromJson(json, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
