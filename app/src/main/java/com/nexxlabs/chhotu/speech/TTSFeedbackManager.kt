package com.nexxlabs.chhotu.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for Text-to-Speech feedback.
 * Provides voice feedback to the user for all command outcomes.
 */
@Singleton
class TTSFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {
    
    companion object {
        private const val TAG = "TTSFeedbackManager"
    }
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()
    
    init {
        tts = TextToSpeech(context, this)
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            isInitialized = when (result) {
                TextToSpeech.LANG_MISSING_DATA,
                TextToSpeech.LANG_NOT_SUPPORTED -> {
                    Log.e(TAG, "Language not supported")
                    false
                }
                else -> {
                    setupUtteranceListener()
                    true
                }
            }
        } else {
            Log.e(TAG, "TTS initialization failed")
            isInitialized = false
        }
    }
    
    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }
            
            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }
            
            @Deprecated("Deprecated in API level 21")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                Log.e(TAG, "TTS error for utterance: $utteranceId")
            }
            
            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                Log.e(TAG, "TTS error for utterance: $utteranceId, code: $errorCode")
            }
        })
    }
    
    /**
     * Speak a message to the user.
     * 
     * @param message The text to speak
     * @param flush If true, stops any current speech and speaks immediately
     */
    fun speak(message: String, flush: Boolean = true) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized, cannot speak: $message")
            return
        }
        
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val utteranceId = UUID.randomUUID().toString()
        
        tts?.speak(message, queueMode, null, utteranceId)
    }
    
    /**
     * Stop any current speech.
     */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }
    
    /**
     * Release TTS resources. Should be called when no longer needed.
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
