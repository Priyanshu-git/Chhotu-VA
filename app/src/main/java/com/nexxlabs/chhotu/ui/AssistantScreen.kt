package com.nexxlabs.chhotu.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexxlabs.chhotu.data.local.CommandHistoryItem
import com.nexxlabs.chhotu.domain.model.Contact
import com.nexxlabs.chhotu.ui.theme.ErrorAccent
import com.nexxlabs.chhotu.ui.theme.ListeningColorDark
import com.nexxlabs.chhotu.ui.theme.ListeningColorLight
import com.nexxlabs.chhotu.ui.theme.ProcessingColorDark
import com.nexxlabs.chhotu.ui.theme.ProcessingColorLight
import com.nexxlabs.chhotu.ui.theme.SuccessAccent
import com.nexxlabs.chhotu.ui.theme.SuccessColorDark
import com.nexxlabs.chhotu.ui.theme.SuccessColorLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Theme-aware status color helpers ────────────────────────────────────────────

@Composable
private fun listeningColor(): Color =
    if (isSystemInDarkTheme()) ListeningColorDark else ListeningColorLight

@Composable
private fun processingColor(): Color =
    if (isSystemInDarkTheme()) ProcessingColorDark else ProcessingColorLight

@Composable
private fun successColor(): Color =
    if (isSystemInDarkTheme()) SuccessColorDark else SuccessColorLight

// ── Main Screen ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
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
    val showOnboarding by viewModel.showOnboarding.collectAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current

    var selectedHistoryItem by remember { mutableStateOf<CommandHistoryItem?>(null) }

    // History action bottom sheet
    if (selectedHistoryItem != null) {
        HistoryActionSheet(
            item = selectedHistoryItem!!,
            onRepeat = {
                viewModel.onTypedCommandChange(it.originalText)
                viewModel.onTypedCommandSubmit()
                selectedHistoryItem = null
            },
            onEdit = {
                viewModel.onTypedCommandChange(it.originalText)
                selectedHistoryItem = null
                focusRequester.requestFocus()
            },
            onDelete = {
                viewModel.deleteHistoryItem(it)
                selectedHistoryItem = null
            },
            onDismiss = { selectedHistoryItem = null }
        )
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Compact header — flush to top
                CompactHeader(onSettingsClick = onSettingsClick)

                Spacer(modifier = Modifier.height(12.dp))

                // Middle content area — fills available space
                AnimatedContent(
                    targetState = state is AssistantState.SelectContact,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically { it / 4 })
                            .togetherWith(fadeOut(tween(200)))
                    },
                    label = "contentSwitch",
                    modifier = Modifier.weight(1f)
                ) { isContactSelection ->
                    if (isContactSelection) {
                        val selectState = state as? AssistantState.SelectContact
                        if (selectState != null) {
                            ContactSelectionSection(
                                contacts = selectState.contacts,
                                originalCommand = selectState.originalCommand,
                                onContactSelected = { viewModel.onContactSelected(it) }
                            )
                        }
                    } else {
                        CommandHistorySection(
                            history = history,
                            onItemClick = { command ->
                                viewModel.onTypedCommandChange(command)
                                focusRequester.requestFocus()
                            },
                            onItemLongClick = { selectedHistoryItem = it },
                            onItemDismissed = { viewModel.deleteHistoryItem(it) },
                            onSuggestionClick = { suggestion ->
                                viewModel.onTypedCommandChange(suggestion)
                                viewModel.onTypedCommandSubmit()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status chip — compact pill
                StatusChip(state = state)

                Spacer(modifier = Modifier.height(16.dp))

                // Hero microphone button
                HeroMicrophoneButton(
                    state = state,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMicClick()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // De-emphasized text input
                CommandInput(
                    value = typedCommand,
                    onValueChange = viewModel::onTypedCommandChange,
                    onSend = {
                        viewModel.onTypedCommandSubmit()
                        focusManager.clearFocus()
                    },
                    focusRequester = focusRequester,
                    enabled = state !is AssistantState.Listening &&
                            state !is AssistantState.Processing
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Onboarding overlay
            if (showOnboarding) {
                OnboardingOverlay(onDismiss = { viewModel.completeOnboarding() })
            }
        }
    }
}

// ── Compact Header ──────────────────────────────────────────────────────────────

@Composable
private fun CompactHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Chhotu",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
        )
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Status Chip ─────────────────────────────────────────────────────────────────

@Composable
private fun StatusChip(state: AssistantState) {
    val statusText = when (state) {
        is AssistantState.Idle -> "Tap the mic to start"
        is AssistantState.Listening -> "Listening..."
        is AssistantState.Processing -> state.recognizedText
        is AssistantState.Success -> state.feedbackMessage
        is AssistantState.Error -> state.errorMessage
        is AssistantState.SelectContact -> "Which one?"
    }

    // Theme-aware accent color (for background tint + icon)
    val accentColor = when (state) {
        is AssistantState.Idle -> MaterialTheme.colorScheme.onSurfaceVariant
        is AssistantState.Listening -> listeningColor()
        is AssistantState.Processing -> processingColor()
        is AssistantState.Success -> successColor()
        is AssistantState.Error -> MaterialTheme.colorScheme.error
        is AssistantState.SelectContact -> processingColor()
    }

    val statusIcon: ImageVector? = when (state) {
        is AssistantState.Idle -> Icons.Default.Mic
        is AssistantState.Listening -> Icons.Default.GraphicEq
        is AssistantState.Processing -> null
        is AssistantState.Success -> Icons.Default.CheckCircle
        is AssistantState.Error -> Icons.Default.Close
        is AssistantState.SelectContact -> Icons.Default.People
    }

    val animatedAccent by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(300),
        label = "statusAccent"
    )

    Surface(
        shape = RoundedCornerShape(50),
        color = animatedAccent.copy(alpha = 0.15f),
        modifier = Modifier.animateContentSize(tween(250))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (statusIcon != null) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = animatedAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            // Text uses onSurface for guaranteed contrast; accent conveys via bg + icon
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Hero Microphone Button ──────────────────────────────────────────────────────

@Composable
private fun HeroMicrophoneButton(state: AssistantState, onClick: () -> Unit) {
    val isListening = state is AssistantState.Listening
    val isProcessing = state is AssistantState.Processing
    val listening = listeningColor()

    // Sonar rings for listening state
    val infiniteTransition = rememberInfiniteTransition(label = "sonar")

    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.8f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Scale"
    )
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.5f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1Alpha"
    )

    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.8f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2Scale"
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.5f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2Alpha"
    )

    val ring3Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.8f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring3Scale"
    )
    val ring3Alpha by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.5f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring3Alpha"
    )

    // Success/error bounce
    val bounceScale by animateFloatAsState(
        targetValue = when (state) {
            is AssistantState.Success -> 1.12f
            is AssistantState.Error -> 1.08f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.6f),
        label = "bounceScale"
    )

    val buttonColor = when {
        isListening -> listening
        isProcessing -> processingColor()
        state is AssistantState.Success -> successColor()
        state is AssistantState.Error -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    val animatedButtonColor by animateColorAsState(
        targetValue = buttonColor,
        animationSpec = tween(300),
        label = "buttonColor"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        // Sonar rings (listening)
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .scale(ring1Scale)
                    .background(
                        color = listening.copy(alpha = ring1Alpha),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .scale(ring2Scale)
                    .background(
                        color = listening.copy(alpha = ring2Alpha),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .scale(ring3Scale)
                    .background(
                        color = listening.copy(alpha = ring3Alpha),
                        shape = CircleShape
                    )
            )
        }

        // Circular progress ring (processing)
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(116.dp),
                color = processingColor(),
                strokeWidth = 3.dp
            )
        }

        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier
                .size(104.dp)
                .scale(bounceScale),
            shape = CircleShape,
            containerColor = animatedButtonColor,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 12.dp,
                pressedElevation = 16.dp
            )
        ) {
            Icon(
                imageVector = when (state) {
                    is AssistantState.Success -> Icons.Default.Check
                    is AssistantState.Error -> Icons.Default.Close
                    else -> Icons.Default.Mic
                },
                contentDescription = when (state) {
                    is AssistantState.Idle -> "Microphone, tap to start listening"
                    is AssistantState.Listening -> "Listening, tap to stop"
                    is AssistantState.Processing -> "Processing command"
                    is AssistantState.Success -> "Command successful"
                    is AssistantState.Error -> "Command failed"
                    is AssistantState.SelectContact -> "Select a contact"
                },
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }
    }
}

// ── Command Input ───────────────────────────────────────────────────────────────

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
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    "or type here...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            enabled = enabled,
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )

        FilledIconButton(
            onClick = onSend,
            enabled = enabled && value.isNotBlank(),
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send Command",
                tint = if (enabled && value.isNotBlank()) Color.White
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

// ── Command History Section ─────────────────────────────────────────────────────

@Composable
private fun CommandHistorySection(
    history: List<CommandHistoryItem>,
    onItemClick: (String) -> Unit,
    onItemLongClick: (CommandHistoryItem) -> Unit,
    onItemDismissed: (CommandHistoryItem) -> Unit,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (history.isNotEmpty()) {
            Text(
                text = "RECENT COMMANDS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(history, key = { it.timestamp }) { item ->
                    HistoryItem(
                        item = item,
                        onClick = { onItemClick(item.originalText) },
                        onLongClick = { onItemLongClick(item) },
                        onDismissed = { onItemDismissed(item) }
                    )
                }
            }
        } else {
            EmptyState(onSuggestionClick = onSuggestionClick)
        }
    }
}

// ── History Item with Swipe-to-Dismiss + Accent Bar ─────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun HistoryItem(
    item: CommandHistoryItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDismissed: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismissed()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            // Use errorContainer for proper contrast in both themes
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        // Subtle card tint based on success/failure + vivid accent bar
        val cardTint = if (item.wasSuccessful) {
            SuccessAccent.copy(alpha = 0.08f)
        } else {
            ErrorAccent.copy(alpha = 0.08f)
        }
        val accentColor = if (item.wasSuccessful) SuccessAccent else ErrorAccent

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1f)
                    .compositeOver(cardTint)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
            ) {
                // Left accent bar — 4dp, vivid color
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(accentColor)
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 16.dp, top = 14.dp, bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "\u201C${item.originalText}\u201D",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = formatRelativeTime(item.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.feedbackMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ── Empty State with Suggestion Chips ───────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmptyState(onSuggestionClick: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Decorative waveform bars
            WaveformDecoration()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TRY SAYING",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Search Android on Google",
                    "Play Faint on Spotify",
                    "Open WhatsApp",
                    "Call Mom"
                ).forEach { suggestion ->
                    SuggestionChip(
                        onClick = { onSuggestionClick(suggestion) },
                        label = {
                            Text(
                                suggestion,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WaveformDecoration() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(60.dp)
    ) {
        listOf(0.3f, 0.6f, 1f, 0.7f, 0.4f).forEach { heightFraction ->
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight(heightFraction)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

// ── Contact Selection Section ───────────────────────────────────────────────────

@Composable
internal fun ContactSelectionSection(
    contacts: List<Contact>,
    originalCommand: String,
    onContactSelected: (Contact) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Multiple contacts found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Which contact for \u201C$originalCommand\u201D?",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
internal fun ContactItem(contact: Contact, onClick: () -> Unit) {
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
            // Initial avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = contact.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── History Action Bottom Sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryActionSheet(
    item: CommandHistoryItem,
    onRepeat: (CommandHistoryItem) -> Unit,
    onEdit: (CommandHistoryItem) -> Unit,
    onDelete: (CommandHistoryItem) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = "\u201C${item.originalText}\u201D",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            BottomSheetAction(
                icon = Icons.Default.Refresh,
                label = "Repeat this command",
                onClick = { onRepeat(item) }
            )
            BottomSheetAction(
                icon = Icons.Default.Edit,
                label = "Edit and send",
                onClick = { onEdit(item) }
            )
            BottomSheetAction(
                icon = Icons.Default.Delete,
                label = "Delete",
                onClick = { onDelete(item) },
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun BottomSheetAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = tint
        )
    }
}

// ── Onboarding Overlay ──────────────────────────────────────────────────────────

@Composable
private fun OnboardingOverlay(onDismiss: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (step < 2) step++ else onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            val (title, subtitle) = when (step) {
                0 -> "Tap the mic and speak" to "Say commands like \u201COpen WhatsApp\u201D or \u201CCall Mom\u201D"
                1 -> "Or type your command" to "Use the text field for typed commands"
                else -> "You\u2019re all set!" to "Chhotu can open apps, make calls, search, and more"
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (step < 2) "Tap to continue" else "Tap to get started",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.5f)
            )

            // Step indicators
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == step) 10.dp else 8.dp)
                            .background(
                                color = if (index == step) Color.White
                                else Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

// ── Utilities ───────────────────────────────────────────────────────────────────

/**
 * Composite two colors: draws [this] on top of [other], respecting alpha.
 * Simplified version for our card tinting use case.
 */
private fun Color.compositeOver(other: Color): Color {
    val srcA = this.alpha
    val dstA = other.alpha * (1f - srcA)
    val outA = srcA + dstA
    if (outA == 0f) return Color.Transparent
    return Color(
        red = (this.red * srcA + other.red * dstA) / outA,
        green = (this.green * srcA + other.green * dstA) / outA,
        blue = (this.blue * srcA + other.blue * dstA) / outA,
        alpha = outA
    )
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 172_800_000 -> "Yesterday"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
