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
    
    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS
        )
    }

    // Permission launcher for multiple permissions
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (recordAudioGranted) {
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
                        onMicClick = { checkPermissionsAndStartListening() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    private fun checkPermissionsAndStartListening() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            startSpeechRecognition()
        } else {
            // Check if we should show rationale for any of the permissions
            val shouldShowRationale = missingPermissions.any {
                shouldShowRequestPermissionRationale(it)
            }

            if (shouldShowRationale) {
                Toast.makeText(
                    this,
                    "Some permissions are needed for the assistant to fully function",
                    Toast.LENGTH_LONG
                ).show()
            }
            permissionLauncher.launch(missingPermissions.toTypedArray())
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
