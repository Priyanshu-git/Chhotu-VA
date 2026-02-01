package com.nexxlabs.chhotu.execution

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.nexxlabs.chhotu.domain.engine.CapabilityResolver
import com.nexxlabs.chhotu.domain.model.SupportedApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executor for YouTube Music commands.
 * Opens YouTube Music with a search query.
 * Does NOT attempt playback control - only opens search results.
 */
@Singleton
class YouTubeMusicExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val capabilityResolver: CapabilityResolver
) {
    
    companion object {
        private const val YOUTUBE_MUSIC_SEARCH_URL = "https://music.youtube.com/search?q="
    }
    
    /**
     * Execute a YouTube Music search with the given query.
     * 
     * @param query The artist, song, or album to search for
     * @return ExecutionResult indicating success or failure
     */
    fun execute(query: String): ExecutionResult {
//        if (!capabilityResolver.isAppInstalled(SupportedApp.YOUTUBE_MUSIC)) {
//            return ExecutionResult.AppNotInstalled(SupportedApp.YOUTUBE_MUSIC.displayName)
//        }
        
        return try {
            val searchUri = Uri.parse("$YOUTUBE_MUSIC_SEARCH_URL${Uri.encode(query)}")
            val intent = Intent(Intent.ACTION_VIEW, searchUri).apply {
                setPackage(SupportedApp.YOUTUBE_MUSIC.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ExecutionResult.Success("Playing $query on YouTube Music")
        } catch (e: ActivityNotFoundException) {
            // Try opening via browser as fallback
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("$YOUTUBE_MUSIC_SEARCH_URL${Uri.encode(query)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                ExecutionResult.Success("Opening YouTube Music search for $query")
            } catch (e2: Exception) {
                ExecutionResult.Failure("Could not open YouTube Music")
            }
        } catch (e: Exception) {
            ExecutionResult.Failure(e.message ?: "Unknown error")
        }
    }
}
