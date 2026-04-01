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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.nexxlabs.chhotu.speech.SpeechInputManager
import com.nexxlabs.chhotu.ui.theme.ChhotuTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val assistantViewModel: AssistantViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var speechInputManager: SpeechInputManager

    companion object {
        private val ALL_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS
        )
    }

    // Requests all permissions at launch; does not start the mic automatically.
    private val launchPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.RECORD_AUDIO] != true) {
            Toast.makeText(
                this,
                "Microphone permission is required for voice commands",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Used when the user taps the mic button; starts listening after RECORD_AUDIO is granted.
    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.RECORD_AUDIO] == true) {
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
        requestPermissionsOnLaunch()

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            ChhotuTheme(themeMode = themeMode) {
                AppNavGraph(
                    assistantViewModel = assistantViewModel,
                    settingsViewModel = settingsViewModel,
                    onMicClick = ::handleMicClick
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        setupSpeechListener()
    }

    private fun requestPermissionsOnLaunch() {
        val missing = ALL_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            launchPermissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun setupSpeechListener() {
        speechInputManager.setListener(object : SpeechInputManager.SpeechRecognitionListener {
            override fun onReadyForSpeech() {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onEndOfSpeech() {}
            override fun onSpeechRecognized(text: String) {
                assistantViewModel.onSpeechRecognized(text)
            }
            override fun onSpeechError(errorMessage: String) {
                assistantViewModel.onSpeechError(errorMessage)
            }
        })
    }

    private fun handleMicClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startSpeechRecognition()
        } else {
            micPermissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        }
    }

    private fun startSpeechRecognition() {
        assistantViewModel.onStartListening()
        try {
            speechInputManager.startListening()
        } catch (e: Exception) {
            assistantViewModel.onSpeechError("Speech recognition is not available on this device")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechInputManager.destroy()
    }
}
