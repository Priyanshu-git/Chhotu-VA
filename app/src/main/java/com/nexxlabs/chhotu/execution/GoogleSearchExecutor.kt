package com.nexxlabs.chhotu.execution

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.nexxlabs.chhotu.domain.engine.CapabilityResolver
import com.nexxlabs.chhotu.domain.model.CommandIntent
import com.nexxlabs.chhotu.domain.model.SupportedApp
import com.nexxlabs.chhotu.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * Executor for Google Search commands.
 * Opens the Google app with a web search query.
 */
@Singleton
class GoogleSearchExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val capabilityResolver: CapabilityResolver
) : AppExecutor {
    
    companion object {
        private const val GOOGLE_SEARCH_URL = "https://www.google.com/search?q="
    }
    
    override val supportedIntentTypes: Set<KClass<out CommandIntent>> = setOf(
        CommandIntent.Search::class
    )
    
    override fun execute(intent: CommandIntent): ExecutionResult {
        val searchIntent = intent as? CommandIntent.Search
            ?: return ExecutionResult.Failure("Invalid intent type for GoogleSearchExecutor")
        return executeSearch(searchIntent.query)
    }
    
    /**
     * Execute a Google search with the given query.
     * 
     * @param query The search query
     * @return ExecutionResult indicating success or failure
     */
    private fun executeSearch(query: String): ExecutionResult {
        if (!capabilityResolver.isAppInstalled(SupportedApp.GOOGLE_SEARCH)) {
            return ExecutionResult.AppNotInstalled(SupportedApp.GOOGLE_SEARCH.displayName)
        }
        
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                setPackage(SupportedApp.GOOGLE_SEARCH.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ExecutionResult.Success("Searching $query on Google")
        } catch (e: ActivityNotFoundException) {
            // Fallback to browser if Google app can't handle it
            Log.e(Constants.LOG.EXECUTOR, "Opening browser for search: $query", e)
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, 
                    Uri.parse("$GOOGLE_SEARCH_URL${Uri.encode(query)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                ExecutionResult.Success("Searching $query on browser")
            } catch (e2: Exception) {
                ExecutionResult.Failure("Could not open search")
            }
        } catch (e: Exception) {
            ExecutionResult.Failure(e.message ?: "Unknown error")
        }
    }
}
