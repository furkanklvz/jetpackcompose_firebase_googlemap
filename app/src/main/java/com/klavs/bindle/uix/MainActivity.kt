package com.klavs.bindle

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.klavs.bindle.data.entity.sealedclasses.BottomNavItem
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.BindleTheme
import com.klavs.bindle.uix.view.event.Events
import com.klavs.bindle.uix.view.Home
import com.klavs.bindle.uix.view.auth.CreateAccount
import com.klavs.bindle.uix.view.auth.CreateUserSetPassword
import com.klavs.bindle.uix.view.auth.Greeting
import com.klavs.bindle.uix.view.auth.LogIn
import com.klavs.bindle.uix.view.communities.Communities
import com.klavs.bindle.uix.view.communities.CreateCommunity
import com.klavs.bindle.uix.view.communities.communityPage.ActiveEventsListPage
import com.klavs.bindle.uix.view.communities.communityPage.CommunityPage
import com.klavs.bindle.uix.view.communities.communityPage.CreatePost
import com.klavs.bindle.uix.view.communities.communityPage.Post
import com.klavs.bindle.uix.view.event.EditEvent
import com.klavs.bindle.uix.view.event.EventChat
import com.klavs.bindle.uix.view.event.EventPage
import com.klavs.bindle.uix.view.map.CreateEvent
import com.klavs.bindle.uix.view.map.Map
import com.klavs.bindle.uix.view.menu.AppSettings
import com.klavs.bindle.uix.view.menu.LanguageSettings
import com.klavs.bindle.uix.view.menu.Menu
import com.klavs.bindle.uix.view.menu.Profile
import com.klavs.bindle.uix.view.menu.ResetEmail
import com.klavs.bindle.uix.view.menu.ResetPassword
import com.klavs.bindle.uix.view.menu.SupportAndFeedbackPage
import com.klavs.bindle.uix.view.menu.ThemeSettings
import com.klavs.bindle.uix.viewmodel.HomeViewModel
import com.klavs.bindle.uix.viewmodel.LogInViewModel
import com.klavs.bindle.uix.viewmodel.MapViewModel
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import com.klavs.bindle.uix.viewmodel.ProfileViewModel
import com.klavs.bindle.uix.viewmodel.SupportAndFeedbackViewmodel
import com.klavs.bindle.uix.viewmodel.communities.CommunityEventListViewModel
import com.klavs.bindle.uix.viewmodel.communities.CommunityPageViewModel
import com.klavs.bindle.uix.viewmodel.communities.CommunityViewModel
import com.klavs.bindle.uix.viewmodel.communities.PostViewModel
import com.klavs.bindle.uix.viewmodel.event.EditEventViewModel
import com.klavs.bindle.uix.viewmodel.event.EventChatViewModel
import com.klavs.bindle.uix.viewmodel.event.EventsViewModel
import com.klavs.bindle.util.startdestination.StartDestinationProvider
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var startDestinationProvider: StartDestinationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (startDestination, startDestinationArg) = startDestinationProvider.determineStartDestination(
            intent
        )

        enableEdgeToEdge()
        setContent {
            BindleTheme {
                NavHostWithBottomNavigation(
                    startDestination = startDestination,
                    startDestinationArg = startDestinationArg
                )
            }
        }
    }
}

@Composable
private fun NavHostWithBottomNavigation(
    startDestination: String,
    startDestinationArg: String? = null
) {
    var privacyPolicyExpanded by remember { mutableStateOf(false) }
    var termsOfServiceExpanded by remember { mutableStateOf(false) }
    var termsOfServiceChecked by remember { mutableStateOf(false) }
    var privacyPolicyChecked by remember { mutableStateOf(false) }
    var termsExpanded by remember { mutableStateOf(false) }
    var successfullyPurchasedProduct by remember { mutableStateOf<String?>(null) }
    val navHostViewModel: NavHostViewModel = hiltViewModel()
    val currentUser by navHostViewModel.currentUser.collectAsState()
    val userResource by navHostViewModel.userResourceFlow.collectAsState()
    val purchaseResource by navHostViewModel.purchase.collectAsState()
    val context = LocalContext.current
    var launched by rememberSaveable { mutableStateOf(false) }
    Log.e(
        "navHost",
        "startDestination: $startDestination\nstartDestinationArg: $startDestinationArg"
    )
    val navController = rememberNavController()
    var bottomBarIsEnable by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(purchaseResource) {
        Log.d("billing error", "purchaseResource changed: $purchaseResource")
        if (purchaseResource is Resource.Success) {
            successfullyPurchasedProduct = purchaseResource.data!!.products.first()
        }
    }

    LaunchedEffect(userResource) {
        Log.e("mainActivity", "termsExpanded: $termsExpanded")
        Log.e("time deneme", "1732818600000: ${Timestamp(Date(1732818600000))}")
        Log.e("time deneme", "now: ${Timestamp.now()}, ${Timestamp.now().toDate().time}")
        if (userResource is Resource.Success && userResource.data != null) {
            termsExpanded = !userResource.data!!.acceptedTermsAndPrivacyPolicy
        }
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(visible = bottomBarIsEnable) {
                BottomNavigationBar(
                    navController = navController,
                    currentUser = currentUser
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LaunchedEffect(bottomBarIsEnable) {
            Log.e("bottombar", "bottom bar: $bottomBarIsEnable")
        }
        if (successfullyPurchasedProduct != null) {
            PurchaseSuccessfulAlertDialog(
                onDismiss = { successfullyPurchasedProduct = null },
                product = successfullyPurchasedProduct!!
            )
        }
        if (termsOfServiceExpanded) {
            AlertDialog(
                modifier = Modifier.zIndex(3f),
                icon = {
                    Icon(imageVector = Icons.Rounded.PrivacyTip, contentDescription = "")
                },
                onDismissRequest = { termsOfServiceExpanded = false },
                dismissButton = {
                    IconButton(
                        onClick = { termsOfServiceExpanded = false }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "dismiss"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            termsOfServiceChecked = true
                            termsOfServiceExpanded = false
                        }
                    ) {
                        Text(stringResource(R.string.accept))
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.terms_of_service_label)
                    )


                },
                text =
                {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.8f)
                            .verticalScroll(
                                rememberScrollState()
                            )
                    ) {
                        Text(
                            text = stringResource(R.string.terms_of_service)
                        )
                    }

                }
            )
        }
        if (privacyPolicyExpanded) {
            AlertDialog(
                modifier = Modifier.zIndex(3f),
                icon = {
                    Icon(imageVector = Icons.Rounded.PrivacyTip, contentDescription = "")
                },
                onDismissRequest = { privacyPolicyExpanded = false },
                dismissButton = {
                    IconButton(
                        onClick = { privacyPolicyExpanded = false }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "dismiss"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            privacyPolicyChecked = true
                            privacyPolicyExpanded = false
                        }
                    ) {
                        Text(stringResource(R.string.accept))
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.privacy_policy_label)
                    )


                },
                text =
                {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.8f)
                            .verticalScroll(
                                rememberScrollState()
                            )
                    ) {
                        Text(
                            text = stringResource(R.string.privacy_policy)
                        )
                    }

                }
            )
        }
        if (termsExpanded) {
            AlertDialog(
                modifier = Modifier.zIndex(2f),
                icon = { Icon(imageVector = Icons.Rounded.PrivacyTip, contentDescription = "") },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                ),
                onDismissRequest = {},
                confirmButton = {
                    Button(
                        onClick = {
                            if (privacyPolicyChecked && termsOfServiceChecked && userResource.data != null) {
                                navHostViewModel.acceptTermsAndPrivacyPolicy(
                                    uid = userResource.data!!.uid!!
                                )
                            }
                        }
                    ) {
                        Text(stringResource(R.string.accept))
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = termsOfServiceChecked,
                                onCheckedChange = { termsOfServiceChecked = it }
                            )
                            val termsOfServiceText = buildAnnotatedString {
                                append(stringResource(R.string.read_and_accept_terms_of_service_append_one))
                                withStyle(
                                    style = SpanStyle(
                                        color = ButtonDefaults.textButtonColors().contentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(stringResource(R.string.terms_of_service_label))
                                }
                                append(stringResource(R.string.read_and_accept_terms_of_service_append_two))
                            }
                            Text(
                                termsOfServiceText,
                                modifier = Modifier.clickable {
                                    termsOfServiceExpanded = true
                                },
                                style = MaterialTheme.typography.bodySmall
                            )


                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = privacyPolicyChecked,
                                onCheckedChange = { privacyPolicyChecked = it }
                            )
                            val privacyPolicyText = buildAnnotatedString {
                                append(stringResource(R.string.read_and_accept_privacy_policy_append_one))
                                withStyle(
                                    style = SpanStyle(
                                        color = ButtonDefaults.textButtonColors().contentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(stringResource(R.string.privacy_policy_label))
                                }
                                append(stringResource(R.string.read_and_accept_privacy_policy_append_two))
                            }
                            Text(
                                privacyPolicyText,
                                modifier = Modifier.clickable {
                                    privacyPolicyExpanded = true
                                },
                                style = MaterialTheme.typography.bodySmall
                            )


                        }
                    }
                }
            )
        }

        LaunchedEffect(currentUser) {
            if (launched) {
                if (currentUser == null) {
                    navHostViewModel.listenToUserJob?.cancel()
                    navHostViewModel.listenToPurchaseJob?.cancel()
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } else {
                    navHostViewModel.startToListenUserDocument(
                        uid = currentUser!!.uid
                    )
                }
            } else {
                launched = true
                if (currentUser != null) {
                    navHostViewModel.startToListenUserDocument(
                        uid = currentUser!!.uid
                    )
                }
            }
        }
        val startWithCommunity = startDestination == "community_page_graph"
        val navHostStartDestination =
            if (startWithCommunity) startDestination else startDestinationArg?.let { arg ->
                "${startDestination}/$arg"
            } ?: startDestination

        NavHost(
            navController = navController,
            startDestination = navHostStartDestination,
            modifier = Modifier.padding(
                bottom = if (bottomBarIsEnable) innerPadding.calculateBottomPadding()
                else WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200))
            }
        ) {
            composable(BottomNavItem.Home.route) {
                LaunchedEffect(true) {
                    bottomBarIsEnable = true
                }
                val homeViewModel = hiltViewModel<HomeViewModel>()
                Home(
                    navController = navController,
                    currentUser = currentUser,
                    navHostViewModel = navHostViewModel,
                    homeViewModel = homeViewModel
                )
            }
            composable(
                BottomNavItem.Communities.route
            ) { backStackEntry ->
                val viewModel = hiltViewModel<CommunityViewModel>(backStackEntry)
                Communities(
                    navController = navController,
                    currentUser = currentUser,
                    navHostViewModel = navHostViewModel,
                    onBottomBarVisibilityChange = { enable ->
                        bottomBarIsEnable = enable
                        Log.e("communities", "bottom bar: $enable")
                    },
                    viewModel = viewModel
                )
            }
            navigation(startDestination = BottomNavItem.Map.route, route = "map_graph") {
                composable(BottomNavItem.Map.route) { navBackStackEntry ->
                    val parentEntry: NavBackStackEntry = remember(navBackStackEntry) {
                        navController.getBackStackEntry("map_graph")
                    }
                    val mapViewModel = hiltViewModel<MapViewModel>(parentEntry)
                    Map(
                        navController = navController, viewModel = mapViewModel,
                        onBottomBarVisibilityChange = { bottomBarIsEnable = it },
                        currentUser = currentUser,
                        navHostViewModel = navHostViewModel
                    )
                }
                composable("create_event/{latitude}/{longitude}",
                    arguments = listOf(navArgument("latitude") { type = NavType.StringType },
                        navArgument("longitude") { type = NavType.StringType }
                    )
                ) { navBackStackEntry ->
                    if (currentUser != null) {
                        val parentEntry: NavBackStackEntry = remember(navBackStackEntry) {
                            navController.getBackStackEntry("map_graph")
                        }
                        val mapViewModel = hiltViewModel<MapViewModel>(parentEntry)
                        LaunchedEffect(true) {
                            bottomBarIsEnable = false
                        }
                        CreateEvent(
                            navController = navController,
                            viewModel = mapViewModel,
                            latitude = navBackStackEntry.arguments?.getString("latitude")!!,
                            longitude = navBackStackEntry.arguments?.getString("longitude")!!,
                            myUid = currentUser!!.uid,
                            navHostViewModel = navHostViewModel
                        )
                    }
                }
            }

            composable(BottomNavItem.Events.route) {
                LaunchedEffect(true) {
                    bottomBarIsEnable = true
                }
                val viewModel = hiltViewModel<EventsViewModel>(it)
                Events(
                    navController = navController,
                    myUid = currentUser?.uid,
                    navHostViewModel = navHostViewModel,
                    viewModel = viewModel
                )

            }
            composable(BottomNavItem.Menu.route) {
                LaunchedEffect(true) {
                    bottomBarIsEnable = true
                }
                Menu(
                    navController = navController,
                    currentUser = currentUser
                )
            }
            composable("log_in") {
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                val viewModel = hiltViewModel<LogInViewModel>(it)
                LogIn(
                    navController = navController,
                    viewModel = viewModel
                )
            }
            composable("create_user") {
                CreateAccount(navController = navController)
            }
            composable("greeting") {
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                Greeting(
                    navController = navController
                )
            }
            composable(
                "create_user_phase_three/{profilePictureUri}/{userName}/{email}",
                arguments = listOf(navArgument("profilePictureUri") { type = NavType.StringType },
                    navArgument("userName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )) {
                val decodedEmail = Uri.decode(it.arguments?.getString("email")!!)
                val decodedUserName = Uri.decode(it.arguments?.getString("userName")!!)
                CreateUserSetPassword(
                    navController = navController,
                    profilePictureUri = it.arguments?.getString("profilePictureUri") ?: "default",
                    userName = decodedUserName,
                    email = decodedEmail
                )
            }
            composable("menu_profile") {
                val viewModel = hiltViewModel<ProfileViewModel>(it)
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                Profile(
                    navController = navController,
                    viewModel = viewModel,
                    navHostViewModel = navHostViewModel
                )
            }
            composable("reset_password") {
                if (currentUser != null) {
                    val viewModel = hiltViewModel<ProfileViewModel>(it)
                    ResetPassword(
                        navController = navController,
                        currentUser = currentUser!!,
                        viewModel = viewModel,
                    )
                }

            }
            composable("reset_email") {
                val viewModel = hiltViewModel<ProfileViewModel>(it)
                ResetEmail(
                    navController = navController,
                    myUid = currentUser?.uid,
                    viewModel = viewModel
                )
            }
            composable("app_settings") {
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                AppSettings(navController = navController)
            }
            composable("theme") {
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                ThemeSettings(navController = navController)
            }
            composable("language") {
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                LanguageSettings(
                    navController = navController
                )
            }
            composable("support_and_feedback") {
                LaunchedEffect(true) {
                    bottomBarIsEnable = false
                }
                val viewModel = hiltViewModel<SupportAndFeedbackViewmodel>(it)
                SupportAndFeedbackPage(
                    navController = navController,
                    currentUser = currentUser,
                    viewmodel = viewModel
                )
            }
            composable("create_community") {
                if (currentUser != null) {
                    LaunchedEffect(true) {
                        bottomBarIsEnable = false
                    }
                    val viewModel = hiltViewModel<CommunityViewModel>(it)
                    CreateCommunity(
                        navController = navController,
                        myUid = currentUser!!.uid,
                        navHostViewModel = navHostViewModel,
                        viewModel = viewModel,
                    )
                }
            }

            composable(
                "event_chat/{eventId}/{numOfParticipants}",
                arguments = listOf(navArgument("eventId") { type = NavType.StringType },
                    navArgument("numOfParticipants") { type = NavType.IntType }
                ),
                deepLinks = listOf(navDeepLink {
                    uriPattern = "bindle://event_chat/{eventId}/{numOfParticipants}"
                })
            ) { navBackStackEntry ->
                if (currentUser != null) {
                    LaunchedEffect(true) {
                        bottomBarIsEnable = false
                    }
                    val viewModel = hiltViewModel<EventChatViewModel>(navBackStackEntry)
                    EventChat(
                        navController = navController,
                        eventId = navBackStackEntry.arguments?.getString("eventId")!!,
                        numOfParticipants = navBackStackEntry.arguments?.getInt("numOfParticipants")
                            ?: -1,
                        currentUser = currentUser!!,
                        chatViewModel = viewModel
                    )
                }
            }

            composable(
                route = "event_page/{eventId}",
                arguments = listOf(navArgument("eventId") {
                    type = NavType.StringType
                }),
                deepLinks = listOf(navDeepLink { uriPattern = "bindle://event_page/{eventId}" })
            ) { navBackStackEntry ->
                if (currentUser != null) {
                    LaunchedEffect(true) {
                        bottomBarIsEnable = false
                    }
                    EventPage(
                        navController = navController,
                        eventId = navBackStackEntry.arguments?.getString("eventId")!!,
                        currentUser = currentUser!!,
                        navHostViewModel = navHostViewModel
                    )
                }
            }
            composable(
                "post/{communityId}/{postId}",
                arguments = listOf(
                    navArgument("communityId") { type = NavType.StringType },
                    navArgument("postId") { type = NavType.StringType }),
                deepLinks = listOf(navDeepLink {
                    uriPattern = "bindle://post/{communityId}/{postId}"
                })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId")
                val communityId = backStackEntry.arguments?.getString("communityId")
                if (currentUser != null && postId != null && communityId != null) {
                    LaunchedEffect(true) {
                        bottomBarIsEnable = false
                    }
                    val postViewModel = hiltViewModel<PostViewModel>(backStackEntry)
                    Post(
                        navController = navController,
                        postId = postId,
                        viewModel = postViewModel,
                        communityId = communityId,
                        currentUser = currentUser!!
                    )
                }
            }

            navigation(
                startDestination = if (startWithCommunity) {
                    "community_page/$startDestinationArg"
                } else "community_page/{communityId}",
                route = "community_page_graph"
            ) {
                composable(
                    "community_page/{communityId}",
                    arguments = listOf(
                        navArgument("communityId") { type = NavType.StringType }),
                    deepLinks = listOf(navDeepLink {
                        uriPattern = "bindle://community_page/{communityId}"
                    })
                ) { backStackEntry ->
                    LaunchedEffect(true) {
                        bottomBarIsEnable = false
                    }

                    val parentEntry: NavBackStackEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("community_page_graph")
                    }
                    val postViewModel = hiltViewModel<PostViewModel>(parentEntry)
                    val communityPageViewModel = hiltViewModel<CommunityPageViewModel>(parentEntry)
                    CommunityPage(
                        navController = navController,
                        vmPost = postViewModel,
                        communityId = backStackEntry.arguments?.getString("communityId")!!,
                        currentUser = currentUser,
                        viewModel = communityPageViewModel,
                        navHostViewModel = navHostViewModel
                    )

                }
                composable(
                    "createPost/{communityId}",
                    arguments = listOf(navArgument("communityId") { type = NavType.StringType })
                ) { backStackEntry ->
                    if (currentUser != null) {
                        val parentEntry: NavBackStackEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("community_page_graph")
                        }
                        val communityPageViewModel =
                            hiltViewModel<CommunityPageViewModel>(parentEntry)
                        LaunchedEffect(true) {
                            bottomBarIsEnable = false
                        }
                        CreatePost(
                            navController = navController,
                            communityId = backStackEntry.arguments?.getString("communityId")!!,
                            currentUser = currentUser!!,
                            viewModel = communityPageViewModel
                        )
                    }
                }
            }




            composable(
                "edit_event/{event}",
                arguments = listOf(navArgument("event") { type = NavType.StringType })
            ) {
                if (currentUser != null) {
                    val decodedEventJson = Uri.decode(it.arguments?.getString("event")!!)
                    val event = Gson().fromJson(decodedEventJson, Event::class.java)
                    val viewModel = hiltViewModel<EditEventViewModel>(it)
                    EditEvent(
                        navController = navController,
                        myUid = currentUser!!.uid,
                        event = event,
                        viewModel = viewModel,
                        navHostViewModel = navHostViewModel
                    )
                }
            }
            composable("community_events_list_page/{communityId}/{communityName}",
                arguments = listOf(navArgument("communityId") { type = NavType.StringType },
                    navArgument("communityName") { type = NavType.StringType }
                )
            ) { navBackStackEntry ->
                if (currentUser != null) {
                    val eventListViewModel =
                        hiltViewModel<CommunityEventListViewModel>(navBackStackEntry)
                    val decodedCommunityName =
                        Uri.decode(navBackStackEntry.arguments?.getString("communityName")!!)
                    ActiveEventsListPage(
                        navController = navController,
                        communityId = navBackStackEntry.arguments?.getString("communityId")!!,
                        communityName = decodedCommunityName,
                        currentUser = currentUser!!,
                        navHostViewModel = navHostViewModel,
                        eventListViewModel = eventListViewModel
                    )
                }
            }

        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
private fun BottomNavigationBar(navController: NavHostController, currentUser: FirebaseUser?) {
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Communities,
        BottomNavItem.Map,
        BottomNavItem.Events,
        BottomNavItem.Menu
    )

    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route
        bottomNavItems.forEach { bottomBarItem ->
            val selected = currentRoute?.split("/")?.first() == bottomBarItem.route
            Log.d("bottombar", "currentRoute: $currentRoute")
            Log.d("bottombar", "bottomBarItem.route: ${bottomBarItem.route}")
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (bottomBarItem.route != "map" || !selected) {
                        navController.navigate(
                            when (bottomBarItem) {
                                BottomNavItem.Map -> "map_graph"
                                else -> bottomBarItem.route
                            }
                        ) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                icon = if (bottomBarItem is BottomNavItem.Menu) {
                    {
                        BadgedBox(
                            badge = {
                                if (currentUser?.isEmailVerified == false) {
                                    Badge()
                                }
                            }
                        ) { if (selected) bottomBarItem.selectedIcon() else bottomBarItem.unselectedIcon() }
                    }
                } else {
                    if (selected) bottomBarItem.selectedIcon else bottomBarItem.unselectedIcon
                },
                label = {
                    Text(
                        text = stringResource(bottomBarItem.labelResource),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall
                    )
                })
        }
    }
}

@Composable
private fun PurchaseSuccessfulAlertDialog(
    onDismiss: () -> Unit,
    product: String
) {
    val productLabel = when (product) {
        stringResource(R.string.ten_tickets) -> stringResource(R.string.x_tickets, 10)
        stringResource(R.string.twenty_tickets) -> stringResource(R.string.x_tickets, 20)
        stringResource(R.string.thirty_tickets) -> stringResource(R.string.x_tickets, 30)
        else -> ""
    }

    AlertDialog(
        modifier = Modifier.zIndex(4f),
        icon = {
            Icon(imageVector = Icons.Rounded.Check, contentDescription = "purchased")
        },
        title = {
            Text(stringResource(R.string.purchase_successful))
        },
        text = {
            Text(
                text = stringResource(R.string.x_has_been_loaded_to_your_account, productLabel)
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.great))
            }
        },
        onDismissRequest = onDismiss
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    var privacyPolicyExpanded by remember { mutableStateOf(false) }
    var termsOfServiceExpanded by remember { mutableStateOf(false) }
    var termsOfServiceChecked by remember { mutableStateOf(false) }
    var privacyPolicyChecked by remember { mutableStateOf(false) }
    AlertDialog(
        icon = { Icon(imageVector = Icons.Rounded.PrivacyTip, contentDescription = "") },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        onDismissRequest = {},
        confirmButton = {
            Button(
                onClick = {}
            ) {
                Text(stringResource(R.string.accept))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = termsOfServiceChecked,
                        onCheckedChange = { termsOfServiceChecked = it }
                    )
                    val termsOfServiceText = buildAnnotatedString {
                        append(stringResource(R.string.read_and_accept_terms_of_service_append_one))
                        withStyle(
                            style = SpanStyle(
                                color = ButtonDefaults.textButtonColors().contentColor,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(stringResource(R.string.terms_of_service_label))
                        }
                        append(stringResource(R.string.read_and_accept_terms_of_service_append_two))
                    }
                    Text(
                        termsOfServiceText,
                        modifier = Modifier.clickable {
                            termsOfServiceExpanded = true
                        },
                        style = MaterialTheme.typography.bodySmall
                    )


                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = privacyPolicyChecked,
                        onCheckedChange = { privacyPolicyChecked = it }
                    )
                    val privacyPolicyText = buildAnnotatedString {
                        append(stringResource(R.string.read_and_accept_privacy_policy_append_one))
                        withStyle(
                            style = SpanStyle(
                                color = ButtonDefaults.textButtonColors().contentColor,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(stringResource(R.string.privacy_policy_label))
                        }
                        append(stringResource(R.string.read_and_accept_privacy_policy_append_two))
                    }
                    Text(
                        privacyPolicyText,
                        modifier = Modifier.clickable {
                            privacyPolicyExpanded = true
                        },
                        style = MaterialTheme.typography.bodySmall
                    )


                }
            }
        }
    )
}