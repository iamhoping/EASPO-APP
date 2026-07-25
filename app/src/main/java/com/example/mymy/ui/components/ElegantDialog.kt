package com.example.mymy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mymy.ui.theme.DeepGreen
import com.example.mymy.ui.theme.ErrorColor

enum class DialogType {
    ERROR, WARNING, INFO, SUCCESS
}

@Composable
fun ElegantDialog(
    onDismiss: () -> Unit,
    title: String,
    message: String,
    type: DialogType = DialogType.INFO,
    confirmButtonText: String? = "OK",
    onConfirm: (() -> Unit)? = null,
    dismissButtonText: String? = "Cancel",
    onSecondaryAction: (() -> Unit)? = null,
    icon: ImageVector? = null,
    showCloseButton: Boolean = true
) {
    val iconColor = when (type) {
        DialogType.ERROR -> Color(0xFFE53935)
        DialogType.WARNING -> Color(0xFFFFB300)
        DialogType.INFO -> Color(0xFF1E88E5)
        DialogType.SUCCESS -> Color(0xFF43A047)
    }

    val defaultIcon = when (type) {
        DialogType.ERROR -> Icons.Default.ErrorOutline
        DialogType.WARNING -> Icons.Default.Warning
        DialogType.INFO -> Icons.Default.Info
        DialogType.SUCCESS -> Icons.Default.CheckCircleOutline
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Box(modifier = Modifier.padding(28.dp)) {
                if (showCloseButton) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 16.dp, y = (-16).dp)
                    ) {
                        Icon(
                            Icons.Default.Close, 
                            contentDescription = "Close", 
                            tint = Color.Gray.copy(alpha = 0.6f), 
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon with soft glow/gradient effect
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                color = iconColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Icon(
                            imageVector = icon ?: defaultIcon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1C1E),
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF42474E),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (dismissButtonText != null) {
                            OutlinedButton(
                                onClick = { 
                                    onSecondaryAction?.invoke() ?: onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(18.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF0F0F0)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF42474E))
                            ) {
                                Text(dismissButtonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        if (confirmButtonText != null) {
                            Button(
                                onClick = {
                                    onConfirm?.invoke() ?: onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (type == DialogType.ERROR) iconColor else DeepGreen
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text(confirmButtonText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Common Scenarios Implementation
@Composable
fun ConnectionErrorDialog(onRetry: () -> Unit, onDismiss: () -> Unit) {
    ElegantDialog(
        onDismiss = onDismiss,
        title = "No Internet Connection",
        message = "Your connection seems to be offline. Please check your settings and try again.",
        type = DialogType.WARNING,
        icon = Icons.Default.WifiOff,
        confirmButtonText = "Try Again",
        onConfirm = onRetry
    )
}

@Composable
fun AuthErrorDialog(message: String, onDismiss: () -> Unit) {
    ElegantDialog(
        onDismiss = onDismiss,
        title = "Login Failed",
        message = message,
        type = DialogType.ERROR,
        icon = Icons.Default.LockPerson,
        confirmButtonText = "Try Again",
        dismissButtonText = null
    )
}

@Composable
fun PermissionDialog(title: String, message: String, onEnable: () -> Unit, onDismiss: () -> Unit) {
    ElegantDialog(
        onDismiss = onDismiss,
        title = title,
        message = message,
        type = DialogType.INFO,
        icon = Icons.Default.Security,
        confirmButtonText = "Enable",
        onConfirm = onEnable
    )
}

@Composable
fun SessionExpiredDialog(onLogin: () -> Unit) {
    ElegantDialog(
        onDismiss = onLogin,
        title = "Session Expired",
        message = "Your session has timed out for security. Please log in again to continue.",
        type = DialogType.WARNING,
        icon = Icons.Default.Timer,
        confirmButtonText = "Log In",
        dismissButtonText = null,
        showCloseButton = false
    )
}

@Composable
fun SuccessDialog(title: String, message: String, onConfirm: () -> Unit) {
    ElegantDialog(
        onDismiss = onConfirm,
        title = title,
        message = message,
        type = DialogType.SUCCESS,
        icon = Icons.Default.CheckCircle,
        confirmButtonText = "Great",
        dismissButtonText = null
    )
}

@Composable
fun DeleteConfirmationDialog(itemName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    ElegantDialog(
        onDismiss = onDismiss,
        title = "Confirm Deletion",
        message = "Are you sure you want to delete $itemName? This action cannot be undone.",
        type = DialogType.ERROR,
        icon = Icons.Default.DeleteForever,
        confirmButtonText = "Delete",
        onConfirm = onConfirm,
        dismissButtonText = "Cancel"
    )
}
