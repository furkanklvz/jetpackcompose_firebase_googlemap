package com.klavs.bindle.uix.view.event

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.message.Message
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.Green2
import com.klavs.bindle.uix.view.GlideImageLoader
import com.klavs.bindle.uix.viewmodel.event.EventChatViewModel
import com.klavs.bindle.util.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EventChat(
    eventId: String,
    numOfParticipants: Int,
    currentUser: FirebaseUser,
    navController: NavHostController,
    chatViewModel: EventChatViewModel
) {
    val eventResource by chatViewModel.event.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val pagedMessagesResource by chatViewModel.messages.collectAsState()
    val newMessagesResource by chatViewModel.newMessages.collectAsState()
    val messages = remember { mutableStateListOf<Message>() }
    val messageSentResource by chatViewModel.messageSent.collectAsState()
    var messagesLoaded by remember { mutableStateOf(false) }
    var reportUserDialog by remember { mutableStateOf<Message?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        if (navController.currentBackStackEntry == null) {
            navController.navigate("event_page/$eventId")
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(true) {
        if (numOfParticipants == -1) {
            chatViewModel.getNumOfParticipant(eventId = eventId)
        }
        chatViewModel.listenToEvent(
            eventId = eventId
        )
        Constants.displayedChatId = eventId
    }
    DisposableEffect(Unit) {
        onDispose {
            Constants.displayedChatId = null
        }
    }

    LaunchedEffect(eventResource) {
        if (eventResource is Resource.Success && eventResource.data != null) {
            if (pagedMessagesResource is Resource.Idle) {
                chatViewModel.getMessages(
                    eventId = eventId,
                    pageSize = 10,
                    myUid = currentUser.uid
                )
            }
        }
    }

    LaunchedEffect(pagedMessagesResource.data) {
        if (pagedMessagesResource is Resource.Success && pagedMessagesResource.data != null) {
            messages.addAll(pagedMessagesResource.data!!.sortedByDescending { it.timestamp as? Timestamp })
            messagesLoaded = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    )
                ),
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Group,
                            contentDescription = "count the participants"
                        )
                        Text("${if (numOfParticipants == -1) chatViewModel.numOfParticipant.value ?: "" else numOfParticipants}")
                    }
                    if (eventResource.data?.ownerUid == currentUser.uid) {
                        var expandChatSettings by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { expandChatSettings = true }
                        ) {
                            Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "more")
                        }
                        DropdownMenu(
                            expanded = expandChatSettings,
                            onDismissRequest = { expandChatSettings = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.participants_cannot_message)) },
                                onClick = {},
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Chat,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    Switch(
                                        thumbContent = {
                                            if (eventResource.data?.chatRestriction == true) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Lock,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                                )
                                            }
                                        },
                                        checked = eventResource.data?.chatRestriction == true,
                                        onCheckedChange = { checked ->
                                            chatViewModel.changeChatRestriction(
                                                eventId = eventId,
                                                restriction = checked
                                            )
                                        }
                                    )
                                }
                            )
                        }

                    }
                },
                title = {
                    Text(
                        eventResource.data?.title ?: "", style = MaterialTheme.typography.titleSmall
                    )

                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (navController.currentBackStackEntry == null) {
                                navController.navigate("event_page/$eventId")
                            } else {
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(
                    top = innerPadding.calculateTopPadding()
                )
                .fillMaxSize()

        ) {

            if (reportUserDialog != null){
                var selectedContent by remember { mutableStateOf<Int?>(null) }
                var optionalDescription by remember { mutableStateOf("") }
                val messageLimit = 100
                Dialog(onDismissRequest = { reportUserDialog = null }) {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                stringResource(R.string.report_the_user),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.please_select_one) + ":",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .align(Alignment.Start)
                            )
                            ListItem(
                                modifier = Modifier.clickable { selectedContent = 0 },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = {
                                    Text(
                                        stringResource(R.string.child_abuse_or_illegal_content),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingContent = {
                                    if (selectedContent == 0) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "sexual content"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                modifier = Modifier.clickable { selectedContent = 1 },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = {
                                    Text(
                                        stringResource(R.string.disruptive_behavior_or_harassment),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingContent = {
                                    if (selectedContent == 1) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "violence"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                modifier = Modifier.clickable { selectedContent = 2 },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                headlineContent = {
                                    Text(
                                        stringResource(R.string.something_else),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                trailingContent = {
                                    if (selectedContent == 2) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "something else"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(
                                thickness = 3.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth(0.2f)
                                    .clip(CircleShape)
                            )
                            TextField(
                                label = {
                                    Text(
                                        stringResource(R.string.optional_description),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                value = optionalDescription,
                                minLines = 1,
                                modifier = Modifier.fillMaxWidth(0.9f),
                                supportingText = {
                                    Text("$messageLimit/${optionalDescription.length}")
                                },
                                maxLines = 4,
                                textStyle = TextStyle(
                                    fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                ),
                                onValueChange = {
                                    if (it.length <= messageLimit) {
                                        optionalDescription = it
                                    }
                                }
                            )
                            Row(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { reportUserDialog = null }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "back"
                                    )
                                }
                                OutlinedButton(
                                    enabled = selectedContent != null,
                                    onClick = {
                                        chatViewModel.sendReport(
                                            reportedUser = reportUserDialog!!.senderUid,
                                            uid = currentUser.uid,
                                            reportType = selectedContent!!,
                                            description = optionalDescription,
                                            messageContent = reportUserDialog!!.message,
                                            messageId = reportUserDialog!!.id?:""
                                        )
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                context.getString(R.string.report_sent)
                                            )
                                        }
                                        reportUserDialog = null
                                    }
                                ) {
                                    Text(stringResource(R.string.send))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Send,
                                        contentDescription = "send",
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .size(IconButtonDefaults.xSmallIconSize)
                                    )
                                }
                            }

                        }
                    }
                }
            }
            if (isLoading) {
                CircularWavyProgressIndicator(
                    Modifier
                        .align(Alignment.Center)
                        .zIndex(2f)
                )
            }
            when (eventResource) {
                is Resource.Error -> {
                    isLoading = false
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(imageVector = Icons.Rounded.WarningAmber, contentDescription = "error")
                        Text(
                            eventResource.messageResource?.let { stringResource(it) }?:"",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                is Resource.Idle -> {}

                is Resource.Loading -> {
                    isLoading = true
                }

                is Resource.Success -> {
                    when (newMessagesResource) {
                        is Resource.Error -> {
                            isLoading = false
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = "error"
                                )
                                Text(
                                    newMessagesResource.messageResource?.let { stringResource(it) }?:"",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }

                        is Resource.Idle -> {}

                        is Resource.Loading -> {
                            isLoading = true
                        }

                        is Resource.Success -> {
                            if (pagedMessagesResource is Resource.Success) {
                                messagesLoaded = true
                            }
                            if (messagesLoaded) {
                                isLoading = false
                                Column(
                                    Modifier
                                        .fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (messages.isEmpty() && (newMessagesResource.data
                                            ?: emptyList()).isEmpty()
                                    ) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        ) {
                                            Text(
                                                if (eventResource.data?.ownerUid != currentUser.uid && eventResource.data?.chatRestriction == true) {
                                                    stringResource(R.string.no_messages)
                                                } else {
                                                    stringResource(R.string.start_a_conversation)
                                                },
                                                style = MaterialTheme.typography.titleLarge,
                                                modifier = Modifier.align(
                                                    Alignment.Center
                                                )
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            reverseLayout = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        ) {

                                            items(newMessagesResource.data!!) { message ->
                                                MessageRow(
                                                    message = message,
                                                    currentUser = currentUser,
                                                    eventOwnerUid = eventResource.data?.ownerUid
                                                        ?: "",
                                                    reportUser = {reportUserDialog = message}
                                                )
                                            }
                                            items(messages) { message ->
                                                MessageRow(
                                                    message = message,
                                                    currentUser = currentUser,
                                                    eventOwnerUid = eventResource.data?.ownerUid
                                                        ?: "",
                                                    reportUser = {reportUserDialog = message}
                                                )
                                            }

                                            if (pagedMessagesResource is Resource.Loading) {
                                                item {
                                                    Box(
                                                        Modifier
                                                            .padding(15.dp)
                                                            .fillMaxWidth()
                                                    ) {
                                                        CircularWavyProgressIndicator(
                                                            Modifier
                                                                .size(40.dp)
                                                                .align(
                                                                    Alignment.Center
                                                                )
                                                        )
                                                    }

                                                }
                                            } else {
                                                if (chatViewModel.oldestMessageDoc != null && eventResource.data?.id != null) {
                                                    item {
                                                        Box(
                                                            Modifier
                                                                .padding(15.dp)
                                                                .fillMaxWidth()
                                                        ) {
                                                            IconButton(
                                                                onClick = {
                                                                    chatViewModel.getMessages(
                                                                        eventId = eventResource.data!!.id!!,
                                                                        pageSize = 10,
                                                                        myUid = currentUser.uid
                                                                    )
                                                                },
                                                                modifier = Modifier
                                                                    .size(40.dp)
                                                                    .align(Alignment.Center)
                                                                    .border(
                                                                        1.dp,
                                                                        IconButtonDefaults.iconButtonColors().contentColor,
                                                                        CircleShape
                                                                    )
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Rounded.Add,
                                                                    contentDescription = "load more"
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (eventResource.data?.chatRestriction == true && currentUser.uid != eventResource.data?.ownerUid) {
                                        Surface(
                                            modifier = Modifier.padding(5.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                stringResource(R.string.only_event_owner_can_message),
                                                style = MaterialTheme.typography.bodySmall,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(5.dp)
                                            )
                                        }
                                    } else {
                                        val focusRequester = remember { FocusRequester() }
                                        Row(
                                            modifier = Modifier
                                                .consumeWindowInsets(innerPadding)
                                                .imePadding()
                                                .padding(5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextField(
                                                placeholder = { Text(stringResource(R.string.event_chat_message_placeholder)) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .focusRequester(focusRequester),
                                                shape = CircleShape,
                                                colors = TextFieldDefaults.colors(
                                                    focusedIndicatorColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent,
                                                    disabledIndicatorColor = Color.Transparent
                                                ),
                                                value = messageText,
                                                onValueChange = { messageText = it }
                                            )
                                            IconButton(
                                                enabled = messageText.isNotBlank() && messageSentResource !is Resource.Loading,
                                                onClick = {
                                                    val messageModel = Message(
                                                        message = messageText.trim(),
                                                        senderUid = currentUser.uid,
                                                        timestamp = FieldValue.serverTimestamp()
                                                    )
                                                    chatViewModel.sendMessage(
                                                        eventId = eventResource.data?.id ?: "",
                                                        message = messageModel
                                                    )
                                                    messageText = ""

                                                },
                                                modifier = Modifier
                                                    .padding(start = 5.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        CircleShape
                                                    )
                                            ) {
                                                if (messageSentResource is Resource.Loading) {
                                                    CircularWavyProgressIndicator(
                                                        Modifier.size(
                                                            IconButtonDefaults.smallIconSize
                                                        )
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Rounded.ArrowUpward,
                                                        contentDescription = "send message"
                                                    )
                                                }
                                            }
                                            LaunchedEffect(true) {
                                                focusRequester.requestFocus()
                                            }
                                        }
                                    }
                                }
                            }


                        }
                    }


                }
            }

        }
    }
}

@Composable
fun MessageRow(
    message: Message,
    eventOwnerUid: String,
    currentUser: FirebaseUser?,
    reportUser: () -> Unit
) {
    val context = LocalContext.current
    val isMyMessage = message.senderUid == currentUser?.uid
    val isEventOwner = eventOwnerUid == message.senderUid
    var sentYesterday = false

    val sdf: SimpleDateFormat? = if (message.timestamp == null) {
        null
    } else if (Timestamp.now()
            .toDate().time - ((message.timestamp as? Timestamp)?.toDate()?.time?:0L) < TimeUnit.DAYS.toMillis(
            1
        )
    ) {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    } else {
        if (Timestamp.now()
                .toDate().time - ((message.timestamp as? Timestamp)?.toDate()?.time?:0L) < TimeUnit.DAYS.toMillis(2)
        ) {
            sentYesterday = true
            SimpleDateFormat("HH:mm", Locale.getDefault())
        } else if (Timestamp.now()
                .toDate().time - ((message.timestamp as? Timestamp)?.toDate()?.time?:0L) < TimeUnit.DAYS.toMillis(365)
        ) {
            SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("dd MMM yy, HH:mm", Locale.getDefault())
        }
    }
    Column(Modifier.fillMaxWidth()) {
        if (isMyMessage) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(10.dp)
                    .align(Alignment.End)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(
                            bottomEnd = 3.dp,
                            bottomStart = 20.dp,
                            topEnd = 20.dp,
                            topStart = 20.dp
                        ),
                        color = MaterialTheme.colorScheme.surfaceTint
                    ) {
                        Text(message.message, modifier = Modifier.padding(10.dp))
                    }
                    if (sdf != null) {
                        Text(
                            "${if (sentYesterday) "${stringResource(R.string.yesterday)}, " else ""}${sdf.format((message.timestamp as? Timestamp)?.toDate()?: Date(0))}",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

        } else {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .heightIn(min = 80.dp)
                    .fillMaxWidth(0.8f)
                    .padding(10.dp)
                    .clickable { reportUser() }
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    if (message.senderPhotoUrl != null) {
                        GlideImageLoader(
                            message.senderPhotoUrl,
                            context = context,
                            Modifier.matchParentSize()
                        )
                    } else {
                        Image(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "user",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.LightGray)
                        )
                    }
                }
                Column {
                    Surface(
                        shape = RoundedCornerShape(
                            bottomStart = 3.dp,
                            bottomEnd = 20.dp,
                            topEnd = 20.dp,
                            topStart = 20.dp
                        ),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                start = 10.dp,
                                end = 10.dp,
                                top = 7.dp,
                                bottom = 10.dp
                            )
                        ) {
                            if (isEventOwner) {
                                Text(
                                    stringResource(R.string.event_owner),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Green2.copy(red = 0.2f, blue = 0.2f, green = 0.4f),
                                    modifier = Modifier
                                        .background(Green2, RoundedCornerShape(6.dp))
                                        .padding(1.dp)
                                )
                            }
                            Text(
                                message.senderUsername ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                message.message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                    }
                    if (sdf != null) {
                        Text(
                            "${if (sentYesterday) "${stringResource(R.string.yesterday)}, " else ""}${sdf.format((message.timestamp as? Timestamp)?.toDate()?:Date(0))}",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(bottom = 12.dp)
                        )

                    }

                }

            }
        }
    }


}


@Preview(showSystemUi = true)
@Composable
private fun EventChatPreview() {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        /*MessageRow(
            message = Message(
                message = "merhaba",
                timestamp = Timestamp(
                    LocalDateTime.of(2023, 11, 23, 11, 1, 20, 20).toInstant(
                    ZoneOffset.UTC)),
                senderUsername = "furkan"
            ),
            eventOwnerUid = "uid",
            currentUser = "uid",
            messageSentResource = Resource.Idle()
        )*/
        Text(
            "username",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "message",
            style = MaterialTheme.typography.bodyMedium
        )
    }

}