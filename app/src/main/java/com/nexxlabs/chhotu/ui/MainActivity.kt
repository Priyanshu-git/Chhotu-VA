package com.nexxlabs.chhotu.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.nexxlabs.chhotu.speech.SpeechInputManager
import com.nexxlabs.chhotu.ui.theme.ChhotuTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main activity for Chhotu voice assistant.
 * Handles speech recognition result and permission requests.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: AssistantViewModel by viewModels()
    
    @Inject
    lateinit var speechInputManager: SpeechInputManager
    
    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognition()
        } else {
            Toast.makeText(
                this,
                "Microphone permission is required for voice commands",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Setup speech listener
        speechInputManager.setListener(object : SpeechInputManager.SpeechRecognitionListener {
            override fun onReadyForSpeech() {
                // UI is already in Listening state from startSpeechRecognition
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                // We could pass this to viewModel for volume animations later
            }

            override fun onEndOfSpeech() {}

            override fun onSpeechRecognized(text: String) {
                viewModel.onSpeechRecognized(text)
            }

            override fun onSpeechError(errorMessage: String) {
                viewModel.onSpeechError(errorMessage)
            }
        })
        
        setContent {
            ChhotuTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AssistantScreen(
                        viewModel = viewModel,
                        onMicClick = { checkPermissionAndStartListening() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    private fun checkPermissionAndStartListening() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startSpeechRecognition()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(
                    this,
                    "Microphone permission is needed to listen to your voice commands",
                    Toast.LENGTH_LONG
                ).show()
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    private fun startSpeechRecognition() {
        viewModel.onStartListening()
        try {
            speechInputManager.startListening()
        } catch (e: Exception) {
            viewModel.onSpeechError("Speech recognition is not available on this device")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechInputManager.destroy()
    }
}
