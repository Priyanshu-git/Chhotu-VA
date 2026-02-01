package com.nexxlabs.chhotu.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for speech recognition input.
 * Uses Android's SpeechRecognizer API to perform recognition without a system popup.
 */
@Singleton
class SpeechInputManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var listener: SpeechRecognitionListener? = null

    interface SpeechRecognitionListener {
        fun onReadyForSpeech()
        fun onBeginningOfSpeech()
        fun onRmsChanged(rmsdB: Float)
        fun onEndOfSpeech()
        fun onSpeechRecognized(text: String)
        fun onSpeechError(errorMessage: String)
    }

    fun setListener(listener: SpeechRecognitionListener) {
        this.listener = listener
    }

    /**
     * Start the speech recognition process.
     */
    fun startListening() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.startListening(intent)
    }

    /**
     * Stop the speech recognition process.
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    /**
     * Cancel the speech recognition process.
     */
    fun cancelListening() {
        speechRecognizer?.cancel()
    }

    /**
     * Release resources.
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d("SpeechInputManager", "onReadyForSpeech")
            listener?.onReadyForSpeech()
        }

        override fun onBeginningOfSpeech() {
            Log.d("SpeechInputManager", "onBeginningOfSpeech")
            listener?.onBeginningOfSpeech()
        }

        override fun onRmsChanged(rmsdB: Float) {
            listener?.onRmsChanged(rmsdB)
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            Log.d("SpeechInputManager", "onEndOfSpeech")
            listener?.onEndOfSpeech()
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown speech error"
            }
            Log.e("SpeechInputManager", "onError: $errorMessage ($error)")
            listener?.onSpeechError(errorMessage)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull()
            if (text != null) {
                Log.d("SpeechInputManager", "onResults: $text")
                listener?.onSpeechRecognized(text)
            } else {
                listener?.onSpeechError("No text found in results")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
             val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
             val text = matches?.firstOrNull()
             if (text != null) {
                 Log.d("SpeechInputManager", "onPartialResults: $text")
                 // We could potentially update the UI with partial results here if needed
             }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
