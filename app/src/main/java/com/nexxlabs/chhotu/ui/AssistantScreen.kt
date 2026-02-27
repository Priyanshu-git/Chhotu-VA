package com.nexxlabs.chhotu.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexxlabs.chhotu.data.local.CommandHistoryItem
import com.nexxlabs.chhotu.domain.model.Contact
import com.nexxlabs.chhotu.ui.theme.ListeningColor
import com.nexxlabs.chhotu.ui.theme.ProcessingColor
import com.nexxlabs.chhotu.ui.theme.SuccessColor

@Composable
fun AssistantScreen(
        viewModel: AssistantViewModel,
        onMicClick: () -> Unit,
        onSettingsClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val history by viewModel.commandHistory.collectAsState()
    val typedCommand by viewModel.typedCommand.collectAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Header
            AppHeader(onSettingsClick = onSettingsClick)

            Spacer(modifier = Modifier.height(32.dp))

            // Status Display
            StatusDisplay(state = state)

            Spacer(modifier = Modifier.height(24.dp))

            // Command History or Contact Selection
            if (state is AssistantState.SelectContact) {
                ContactSelectionSection(
                    contacts = (state as AssistantState.SelectContact).contacts,
                    onContactSelected = { viewModel.onContactSelected(it) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                CommandHistorySection(
                    history = history,
                    onItemClick = { command ->
                        viewModel.onTypedCommandChange(command)
                        focusRequester.requestFocus()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text Input Field
            CommandInput(
                    value = typedCommand,
                    onValueChange = viewModel::onTypedCommandChange,
                    onSend = {
                        viewModel.onTypedCommandSubmit()
                        focusManager.clearFocus()
                    },
                    focusRequester = focusRequester,
                    enabled =
                            state !is AssistantState.Listening &&
                                    state !is AssistantState.Processing
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Microphone Button
            MicrophoneButton(state = state, onClick = onMicClick)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CommandInput(
        value: String,
        onValueChange: (String) -> Unit,
        onSend: () -> Unit,
        focusRequester: FocusRequester,
        enabled: Boolean
) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { Text("Type your command...") },
                enabled = enabled,
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors =
                        OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor =
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedContainerColor =
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor =
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
        )

        IconButton(
                onClick = onSend,
                enabled = enabled && value.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .background(
                    color =
                        if (enabled && value.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
        ) {
            Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Command",
                    tint =
                            if (enabled && value.isNotBlank()) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun AppHeader(onSettingsClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chhotu",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your Voice Assistant",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusDisplay(state: AssistantState) {
    val (statusText, statusColor) =
            when (state) {
                is AssistantState.Idle ->
                        "Tap the mic to start" to MaterialTheme.colorScheme.onSurfaceVariant
                is AssistantState.Listening -> "Listening..." to ListeningColor
                is AssistantState.Processing ->
                        "Processing: \"${state.recognizedText}\"" to ProcessingColor
                is AssistantState.Success -> state.feedbackMessage to SuccessColor
                is AssistantState.Error -> state.errorMessage to MaterialTheme.colorScheme.error
                is AssistantState.SelectContact -> "Which one?" to ProcessingColor
            }

    val animatedColor by
            animateColorAsState(
                    targetValue = statusColor,
                    animationSpec = tween(300),
                    label = "statusColor"
            )

    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
    ) {
        Text(
                text = statusText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = animatedColor,
                textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CommandHistorySection(
        history: List<CommandHistoryItem>,
        onItemClick: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (history.isNotEmpty()) {
            Text(
                    text = "Recent Commands",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(history) { item ->
                    HistoryItem(item = item, onClick = { onItemClick(item.originalText) })
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                            text = "No commands yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                            text = "Try saying:\n\"Search Android on Google\"\n\"Play Faint on Spotify\"\n\"Open WhatsApp\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(item: CommandHistoryItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        colors =
            CardDefaults.cardColors(
                containerColor = if (item.wasSuccessful) {
                    SuccessColor.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                }
            )
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)) {
            Text(
                text = "\"${item.originalText}\"",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.feedbackMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MicrophoneButton(state: AssistantState, onClick: () -> Unit) {
    val isListening = state is AssistantState.Listening
    val isProcessing = state is AssistantState.Processing

    // Pulsing animation for listening state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by
            infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isListening) 1.15f else 1f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation = tween(600, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                            ),
                    label = "scale"
            )

    val buttonColor =
            when {
                isListening -> ListeningColor
                isProcessing -> ProcessingColor
                state is AssistantState.Success -> SuccessColor
                state is AssistantState.Error -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }

    val animatedButtonColor by
            animateColorAsState(
                    targetValue = buttonColor,
                    animationSpec = tween(300),
                    label = "buttonColor"
            )

    // Glow effect for listening state
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
        // Glow background when listening
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        ListeningColor.copy(
                                            alpha = 0.4f
                                        ),
                                        Color.Transparent
                                    )
                            ),
                        shape = CircleShape
                    )
            )
        }

        FloatingActionButton(
                onClick = onClick,
                modifier = Modifier
                    .size(80.dp)
                    .scale(if (isListening) scale else 1f),
                shape = CircleShape,
                containerColor = animatedButtonColor,
                elevation =
                        FloatingActionButtonDefaults.elevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 12.dp
                        )
        ) {
            Icon(
                    imageVector =
                            when (state) {
                                is AssistantState.Success -> Icons.Default.Check
                                is AssistantState.Error -> Icons.Default.Close
                                else -> Icons.Default.Mic
                            },
                    contentDescription = "Microphone",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
            )
        }
    }
}
@Composable
private fun ContactSelectionSection(
    contacts: List<Contact>,
    onContactSelected: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Select a Contact",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(contacts) { contact ->
                ContactItem(contact = contact, onClick = { onContactSelected(contact) })
            }
        }
    }
}

@Composable
private fun ContactItem(
    contact: Contact,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.ContactPage,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
