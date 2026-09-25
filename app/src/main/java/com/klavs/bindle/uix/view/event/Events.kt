package com.klavs.bindle.uix.view.event

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.klavs.bindle.R
import com.klavs.bindle.util.TimeFunctions
import com.klavs.bindle.data.entity.sealedclasses.BottomNavItem
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.sealedclasses.EventType
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.Green2
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import com.klavs.bindle.uix.viewmodel.event.EventsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Events(navController: NavHostController,
           myUid: String?,
           navHostViewModel: NavHostViewModel,
           viewModel: EventsViewModel) {
    val upcomingEventsResource by navHostViewModel.upcomingEvents.collectAsState()
    val pastEventsResource by viewModel.pastEvents.collectAsState()
    val pastEvents = remember { mutableStateListOf<Event>() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(true) {
        if ((upcomingEventsResource is Resource.Idle || upcomingEventsResource is Resource.Error) && myUid != null) {
            navHostViewModel.listenToUpcomingEvents(
                myUid = myUid
            )
        }
    }
    LaunchedEffect(pastEventsResource) {
        if (pastEventsResource is Resource.Error) {
            scope.launch { snackbarHostState.showSnackbar(pastEventsResource.messageResource?.let { context.getString(it) }?:"") }
        }
    }
    LaunchedEffect(pastEventsResource) {
        if (pastEventsResource is Resource.Success && pastEventsResource.data != null) {
            pastEvents.addAll(pastEventsResource.data!!)
        }
    }


    EventsContent(
        snackbarHostState = snackbarHostState,
        myUid = myUid,
        navigateToSignIn = {navController.navigate("log_in")},
        navigateToEventPage = {navController.navigate("event_page/$it")},
        pastEventsResource = pastEventsResource,
        upcomingEventsResource = upcomingEventsResource,
        pastEvents = pastEvents,
        getPasEvents = {
            if (myUid != null) {
                viewModel.getPastEvents(
                    5,
                    myUid = myUid
                )
            }
        },
        thereIsLastPastEvent = viewModel.lastPastEventParticipantDoc != null,
        resetLastPastEvent = {viewModel.lastPastEventParticipantDoc = null}
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EventsContent(
    snackbarHostState: SnackbarHostState,
    myUid: String?,
    navigateToSignIn: () -> Unit,
    navigateToEventPage: (String) -> Unit,
    pastEventsResource: Resource<List<Event>>,
    upcomingEventsResource: Resource<List<Event>>,
    pastEvents: List<Event>,
    getPasEvents: () -> Unit,
    thereIsLastPastEvent: Boolean,
    resetLastPastEvent :()->Unit,
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(BottomNavItem.Events.labelResource),
                        style = MaterialTheme.typography.titleSmall
                    )
                })
        }) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (myUid == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sign_in_to_see_events),
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedButton(
                        onClick = navigateToSignIn
                    ) {
                        Text(stringResource(R.string.sign_in))
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    val tabTitles =
                        listOf(stringResource(R.string.upcoming), stringResource(R.string.past))
                    var state by rememberSaveable { mutableIntStateOf(0) }
                    PrimaryTabRow(
                        divider = {},
                        selectedTabIndex = state
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            val selected = state == index
                            Tab(selected = selected,
                                onClick = {
                                    state = index
                                    if (state == 1 && (pastEventsResource is Resource.Idle || pastEventsResource is Resource.Error)) {
                                        resetLastPastEvent()
                                        getPasEvents()
                                    }
                                },
                                text = { Text(title) },
                                unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                                icon = {
                                    if (index == 0) {
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Filled.Upcoming,
                                                contentDescription = "Upcoming"
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Outlined.Upcoming,
                                                contentDescription = "Upcoming"
                                            )
                                        }
                                    } else {
                                        if (selected) {
                                            Icon(
                                                imageVector = Icons.Filled.Restore,
                                                contentDescription = "Past"
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Filled.Restore,
                                                contentDescription = "Past"
                                            )
                                        }
                                    }
                                })
                        }
                    }
                    when (state) {
                        0 -> {
                            when (upcomingEventsResource) {
                                is Resource.Error -> {
                                    Box(Modifier.fillMaxSize()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            modifier = Modifier.align(Alignment.Center)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.WarningAmber,
                                                contentDescription = "error"
                                            )
                                            Text(upcomingEventsResource.messageResource?.let {
                                                stringResource(
                                                    it
                                                )
                                            } ?: "")
                                        }
                                    }
                                }

                                is Resource.Idle -> {}
                                is Resource.Loading -> {
                                    Box(Modifier.fillMaxSize()) {
                                        CircularWavyProgressIndicator(Modifier.align(Alignment.Center))
                                    }
                                }

                                is Resource.Success -> {
                                    AnimatedVisibility(visible = true) {
                                        UpcomingEvents(
                                            eventList = upcomingEventsResource.data?.sortedBy { it.date }
                                                ?: emptyList(),
                                            myUid = myUid,
                                            navigateToEventPage = {navigateToEventPage(it)}
                                        )
                                    }

                                }
                            }

                        }

                        1 -> {
                            PastEvents(
                                myUid = myUid,
                                pastEventsResource = pastEventsResource,
                                onLoadMore = getPasEvents,
                                pastEvents = pastEvents.toList(),
                                onEventClick = { navigateToEventPage(it) },
                                thereIsLastPastEvent = thereIsLastPastEvent
                            )


                        }
                    }
                }
            }


        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PastEvents(
    pastEventsResource: Resource<List<Event>>,
    pastEvents: List<Event>,
    myUid: String,
    thereIsLastPastEvent: Boolean,
    onEventClick: (String) -> Unit,
    onLoadMore: () -> Unit
) {

    if (pastEvents.isEmpty() && pastEventsResource is Resource.Success) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.no_past_events),
                style = MaterialTheme.typography.titleMedium
            )
        }

    } else {
        LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            items(pastEvents) { event ->
                PastEventRow(myUid = myUid, event = event, onClick = { onEventClick(event.id!!) })
                if (event != pastEvents.last()) {
                    HorizontalDivider()
                }
            }
            if (pastEventsResource is Resource.Loading) {
                item {
                    CircularWavyProgressIndicator(Modifier.padding(10.dp))
                }
            }
            if (pastEventsResource !is Resource.Loading && thereIsLastPastEvent) {
                item {
                    IconButton(
                        onClick = onLoadMore,
                        modifier = Modifier
                            .padding(5.dp)
                            .border(
                                2.dp, IconButtonDefaults.iconButtonColors().contentColor,
                                CircleShape
                            )
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = "load more")
                    }
                }
            }

        }
    }
}

@Composable
private fun UpcomingEvents(
    eventList: List<Event>,
    myUid: String,
    navigateToEventPage: (String) -> Unit
) {

    if (eventList.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.no_upcoming_events),
                style = MaterialTheme.typography.titleMedium
            )
        }

    } else {
        LazyColumn(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            items(eventList) { event ->
                UpcomingEventRow(
                    event = event, onClick = {
                        navigateToEventPage(event.id!!)
                    },
                    myUid = myUid
                )
                if (event != eventList.last()) {
                    HorizontalDivider()
                }
            }
        }
    }


}

@Composable
private fun PastEventRow(event: Event, onClick: () -> Unit, myUid: String) {
    Row(
        Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth(0.91f)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .weight(1f), contentAlignment = Alignment.Center
        ) {
            Text(
                TimeFunctions().convertTimestampToLocalizeDate(
                    timestamp = event.date,
                    singleLine = false
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight()
        ) {
            Text(
                event.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1.2f)) {
            if (event.ownerUid == myUid) {
                Text(
                    stringResource(R.string.your_event),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .background(Green2, RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    color = Color.DarkGray
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UpcomingEventRow(myUid: String, event: Event, onClick: () -> Unit) {
    Row(
        Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth(0.91f)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .weight(1f), contentAlignment = Alignment.Center
        ) {
            Text(
                TimeFunctions().convertTimestampToLocalizeDate(
                    timestamp = event.date,
                    singleLine = false
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight()
        ) {
            Text(
                event.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            Text(sdf.format(event.date.toDate()), style = MaterialTheme.typography.bodyMedium)
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1.2f)) {
            if (event.ownerUid == myUid) {
                Text(
                    stringResource(R.string.your_event),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(Green2, RoundedCornerShape(7.dp))
                        .padding(2.dp),
                    color = Color.DarkGray
                )
            }

        }

    }
}

@Preview
@Composable
private fun EventsPreview() {
    val pastEvents = listOf(
        Event(
            title = "Bisiklet Turu",
            type = EventType.Sport.value,
            date = Timestamp(
                time = LocalDateTime.of(
                    2024, 12, 25, 16, 30, 0
                ).toInstant(ZoneOffset.UTC)
            )
        ),
        Event(
            title = "Doğum Günü",
            type = EventType.Party.value,
            date = Timestamp(
                time = LocalDateTime.of(
                    2024, 12, 26, 16, 30, 0
                ).toInstant(ZoneOffset.UTC)
            ),
            ownerUid = "0"
        ),
        Event(
            title = "Kan Bağışı",
            type = EventType.Solidarity.value,
            date = Timestamp(
                time = LocalDateTime.of(
                    2024, 12, 26, 16, 30, 0
                ).toInstant(ZoneOffset.UTC)
            )
        ),
        Event(
            title = "Fidan Dikme",
            type = EventType.Nature.value,
            date = Timestamp(
                time = LocalDateTime.of(
                    2025, 12, 24, 16, 30, 0
                ).toInstant(ZoneOffset.UTC)
            )
        ),
    ).sortedBy { it.date }

    EventsContent(
        snackbarHostState = remember { SnackbarHostState() },
        myUid = "0",
        navigateToSignIn = {},
        navigateToEventPage = {},
        pastEventsResource = Resource.Success(pastEvents),
        upcomingEventsResource = Resource.Success(pastEvents),
        pastEvents = pastEvents,
        getPasEvents = {},
        thereIsLastPastEvent = false,
        resetLastPastEvent = {}
    )
}