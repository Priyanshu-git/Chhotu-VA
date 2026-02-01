package com.nexxlabs.chhotu.domain.engine

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Normalizes raw voice input for consistent command processing.
 * 
 * Operations performed:
 * - Convert to lowercase
 * - Trim whitespace
 * - Remove filler words that don't affect command meaning
 */
@Singleton
class CommandNormalizer @Inject constructor() {
    
    companion object {
        /**
         * Words that can be removed from commands without changing meaning.
         * These are common conversational fillers.
         */
        private val FILLER_WORDS = listOf(
            "please",
            "can you",
            "could you",
            "would you",
            "hey",
            "hi",
            "hello",
            "just",
            "kindly",
            "i want to",
            "i'd like to",
            "i would like to"
        )
    }
    
    /**
     * Normalize the input text for command processing.
     * 
     * @param input Raw text from speech recognition
     * @return Normalized text ready for command matching
     */
    fun normalize(input: String): String {
        var result = input
            .lowercase()
            .trim()
        
        // Remove filler words (longer phrases first to avoid partial matches)
//        FILLER_WORDS.sortedByDescending { it.length }.forEach { filler ->
//            result = result.replace(filler, "")
//        }
        
        // Clean up extra whitespace that may result from removals
        result = result.replace(Regex("\\s+"), " ").trim()
        
        return result
    }
}
