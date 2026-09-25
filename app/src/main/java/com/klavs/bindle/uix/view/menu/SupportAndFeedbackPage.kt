package com.klavs.bindle.uix.view.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.FeedbackMessage
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.viewmodel.SupportAndFeedbackViewmodel
import kotlinx.coroutines.launch

@Composable
fun SupportAndFeedbackPage(
    navController: NavHostController,
    currentUser: FirebaseUser?,
    viewmodel: SupportAndFeedbackViewmodel
) {

    val sendingResource by viewmodel.sendingResource.collectAsState()

    SupportAndFeedbackContent(
        onBackClick = { navController.popBackStack() },
        signedInUserEmail = currentUser?.email,
        signedInUserUid = currentUser?.uid,
        sendMessage = { viewmodel.sendFeedback(it) },
        sendingResource = sendingResource,
        resetSendingResource = { viewmodel.resetSendingResource() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SupportAndFeedbackContent(
    onBackClick: () -> Unit,
    signedInUserEmail: String?,
    sendMessage: (FeedbackMessage) -> Unit,
    sendingResource: Resource<FeedbackMessage>,
    resetSendingResource: () -> Unit,
    signedInUserUid: String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var email by remember { mutableStateOf(signedInUserEmail ?: "") }
    var emailIsEmpty by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var messageIsEmpty by remember { mutableStateOf(false) }
    val maxMessageLength = 800

    LaunchedEffect(sendingResource) {
        if (sendingResource is Resource.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(sendingResource.messageResource?.let {
                    context.getString(
                        it
                    )
                } ?: "")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "go back"
                        )
                    }
                },
                title = {
                    Text(
                        stringResource(R.string.support_and_feedback),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            when (sendingResource) {
                is Resource.Loading -> {
                    Dialog(
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false
                        ),
                        onDismissRequest = {}
                    ) {
                        CircularWavyProgressIndicator()
                    }
                }

                is Resource.Success -> {
                    message = ""
                    AlertDialog(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "message sent"
                            )
                        },
                        title = {
                            Text(stringResource(R.string.message_sent))
                        },
                        text = {
                            Text(
                                stringResource(
                                    R.string.your_feedback_or_support_message_has_been_sent,
                                    sendingResource.data?.email ?: ""
                                )
                            )
                        },
                        onDismissRequest = resetSendingResource,
                        confirmButton = {
                            Button(
                                onClick = resetSendingResource
                            ) {
                                Text(stringResource(R.string.okay))
                            }
                        },
                    )
                }

                else -> {}
            }
            Column(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.9f)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    ),
                    isError = emailIsEmpty,
                    label = {
                        Text(stringResource(R.string.email_address) + ":")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    value = email,
                    onValueChange = {
                        emailIsEmpty = false
                        if (it.length <= 200) {
                            email = it.trim()
                        }
                    })
                TextField(
                    supportingText = {
                        Text("${message.length}/$maxMessageLength")
                    },
                    isError = messageIsEmpty,
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    ),
                    label = {
                        Text(stringResource(R.string.your_message) + ":")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    value = message,
                    onValueChange = {
                        messageIsEmpty = false
                        if (it.length <= maxMessageLength) {
                            message = it
                        }
                    },
                    minLines = 8,
                    maxLines = 15
                )
                FilledTonalIconButton(
                    modifier = Modifier
                        .align(Alignment.End)
                        .size(IconButtonDefaults.mediumContainerSize()),
                    onClick = {
                        if (email.isNotBlank() && message.isNotBlank()) {
                            sendMessage(
                                FeedbackMessage(
                                    email = email,
                                    message = message,
                                    uid = signedInUserUid,
                                    timestamp = Timestamp.now()
                                )
                            )
                        } else {
                            emailIsEmpty = email.isBlank()
                            messageIsEmpty = message.isBlank()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send, contentDescription = "send",
                        modifier = Modifier.size(IconButtonDefaults.mediumIconSize)
                    )
                }
            }
        }
    }
}


@Preview
@Composable
private fun SupportAndFeedbackPagePreview() {
    SupportAndFeedbackContent(
        onBackClick = {},
        signedInUserEmail = "",
        sendMessage = {},
        sendingResource = Resource.Idle(
            /*data = FeedbackMessage(
                email = "furkanklvz0@gmail.com"
            )*/
        ),
        signedInUserUid = null,
        resetSendingResource = {}
    )
}