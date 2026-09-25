package com.klavs.bindle.uix.view.menu

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocalActivity
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Transgender
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.klavs.bindle.R
import com.klavs.bindle.util.TimeFunctions
import com.klavs.bindle.data.entity.sealedclasses.Gender
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.viewmodel.NavHostViewModel
import com.klavs.bindle.uix.viewmodel.ProfileViewModel
import com.klavs.bindle.util.TicketDialog
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Date

fun getGenderDisplayFromValue(value: String): Int {
    val genderList = listOf(
        Gender.PreferNotToSay,
        Gender.Male,
        Gender.Female
    )
    return genderList.find { it.value == value }?.titleResource
        ?: Gender.PreferNotToSay.titleResource
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Profile(
    navController: NavHostController,
    viewModel: ProfileViewModel,
    navHostViewModel: NavHostViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()

    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    var realName by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var phoneNumberName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<String?>(null) }
    var birthDay by remember { mutableStateOf<Timestamp?>(null) }
    var realNameChanged by remember { mutableStateOf(false) }
    var userNameChanged by remember { mutableStateOf(false) }
    var phoneNumberChanged by remember { mutableStateOf(false) }
    var genderChanged by remember { mutableStateOf(false) }
    var birthDayChanged by remember { mutableStateOf(false) }
    var userNameIsEmpty by remember { mutableStateOf(false) }
    var userNameAlreadyUsed by remember { mutableStateOf(false) }
    val userResource by navHostViewModel.userResourceFlow.collectAsState()
    val deletingAccountResource by viewModel.deletingAccountResource.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTicketDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser != null && !currentUser!!.isEmailVerified){
            if (viewModel.sendEmailVerificationState.value is Resource.Success) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        context.getString(
                            R.string.please_check_your_email
                        )
                    )
                }
            } else {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        context.getString(R.string.unverified_account),
                        actionLabel = context.getString(R.string.verify_with_email),
                        duration = SnackbarDuration.Long,
                        withDismissAction = true
                    )
                    when (result) {
                        SnackbarResult.Dismissed -> {
                            snackbarHostState.currentSnackbarData?.dismiss()
                        }

                        SnackbarResult.ActionPerformed -> {
                            viewModel.sendEmailVerification()
                            snackbarHostState.currentSnackbarData?.dismiss()
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.popBackStack()
        }
    }
    LaunchedEffect(viewModel.checkUniqueUsernameState.value) {
        when (val state = viewModel.checkUniqueUsernameState.value) {
            is Resource.Error -> {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar(state.messageResource?.let {
                        context.getString(
                            it
                        )
                    } ?: "")
                }
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isLoading = true
            }

            is Resource.Success -> {
                isLoading = false
                if (state.data!!) {
                    val newUserData = hashMapOf<String, Any?>(
                        "userName" to userName.trim()
                    )
                    if (realNameChanged) {
                        newUserData["realName"] = realName.trim()
                    }
                    if (phoneNumberChanged) {
                        newUserData["phoneNumber"] = phoneNumberName.trim()
                    }
                    if (genderChanged) {
                        newUserData["gender"] = gender
                    }
                    if (birthDayChanged) {
                        newUserData["birthDate"] = birthDay
                    }
                    if (currentUser != null) {
                        viewModel.updateUserData(
                            newUserData = newUserData,
                            myUid = currentUser!!.uid
                        )
                    }
                } else {
                    userNameAlreadyUsed = true
                }
            }
        }
    }
    LaunchedEffect(key1 = viewModel.updateUserDataState.value) {
        when (val resource = viewModel.updateUserDataState.value) {
            is Resource.Error -> {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar(resource.messageResource?.let {
                        context.getString(
                            it
                        )
                    } ?: "")
                }
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isLoading = true
            }

            is Resource.Success -> {
                isLoading = false
                navController.popBackStack()
            }
        }
    }
    LaunchedEffect(deletingAccountResource) {
        if (deletingAccountResource is Resource.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    context.getString(deletingAccountResource.messageResource!!)
                )
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            MediumTopAppBar(
                actions = {
                    FilledTonalButton(
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        shape = ButtonDefaults.squareShape,
                        onClick = {
                            showTicketDialog = true
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.LocalActivity,
                                contentDescription = "tickets",
                                modifier = Modifier
                                    .padding(end = 5.dp)
                                    .size(IconButtonDefaults.xSmallIconSize),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(userResource.data?.tickets?.toString() ?: "")
                        }
                    }
                    TextButton(
                        modifier = Modifier.padding(start = 10.dp),
                        onClick = {
                            if (userName.isBlank()) {
                                userNameIsEmpty = true
                            } else {
                                if (userNameChanged) {
                                    viewModel.checkUniqueUsername(
                                        userName = userName.trim(),
                                        myUid = currentUser!!.uid
                                    )
                                } else {
                                    val newUserData = hashMapOf<String, Any?>()
                                    if (realNameChanged) {
                                        newUserData["realName"] = realName
                                    }
                                    if (phoneNumberChanged) {
                                        newUserData["phoneNumber"] = phoneNumberName
                                    }
                                    if (genderChanged) {
                                        newUserData["gender"] = gender
                                    }
                                    if (birthDayChanged) {
                                        newUserData["birthDate"] = birthDay
                                    }
                                    if (currentUser != null) {
                                        viewModel.updateUserData(
                                            newUserData = newUserData,
                                            myUid = currentUser!!.uid
                                        )
                                    }
                                }
                            }
                        }) {
                        Text(
                            text = stringResource(R.string.save),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "turn back"
                        )
                    }
                },
                title = {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val screenWith = LocalConfiguration.current.screenWidthDp.dp
                        Text(
                            text = userResource.data?.userName ?: "",
                            modifier = Modifier.widthIn(max = screenWith * 0.4f)
                        )
                        if (currentUser != null) {
                            if (currentUser!!.isEmailVerified) {
                                Icon(
                                    modifier = Modifier.clickable {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(if (currentUser!!.providerData.map { it.providerId }
                                                    .contains(GoogleAuthProvider.PROVIDER_ID)) context.getString(
                                                R.string.verified_with_google
                                            )
                                            else context.getString(R.string.verified_with_email))
                                        }
                                    },
                                    imageVector = Icons.Rounded.Verified,
                                    contentDescription = "verified"
                                )

                            } else {
                                    FilledTonalIconButton(
                                        modifier = Modifier.size(IconButtonDefaults.xSmallContainerSize()),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        ),
                                        onClick = {
                                            if (viewModel.sendEmailVerificationState.value is Resource.Success) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        context.getString(
                                                            R.string.please_check_your_email
                                                        )
                                                    )
                                                }
                                            } else {
                                                scope.launch {
                                                    val result = snackbarHostState.showSnackbar(
                                                        context.getString(R.string.unverified_account),
                                                        actionLabel = context.getString(R.string.verify_with_email),
                                                        duration = SnackbarDuration.Long,
                                                        withDismissAction = true
                                                    )
                                                    when (result) {
                                                        SnackbarResult.Dismissed -> {
                                                            snackbarHostState.currentSnackbarData?.dismiss()
                                                        }

                                                        SnackbarResult.ActionPerformed -> {
                                                            viewModel.sendEmailVerification()
                                                            snackbarHostState.currentSnackbarData?.dismiss()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(IconButtonDefaults.xSmallIconSize),
                                            painter = painterResource(R.drawable.person_alert),
                                            contentDescription = "verify"
                                        )
                                    }

                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (deletingAccountResource is Resource.Loading) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                CircularWavyProgressIndicator()
            }
        }
        when (userResource) {
            is Resource.Error -> {
                if (deletingAccountResource is Resource.Idle || deletingAccountResource is Resource.Error) {
                    Box(
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userResource.messageResource?.let { context.getString(it) }
                                ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {}

            is Resource.Success -> {
                val user = userResource.data!!
                LaunchedEffect(key1 = true) {
                    realName = user.realName ?: ""
                    userName = user.userName
                    phoneNumberName = user.phoneNumber ?: ""
                    gender = user.gender
                    birthDay = user.birthDate
                }
                if (currentUser != null) {
                    Box {
                        if (isLoading) {
                            Dialog(
                                onDismissRequest = {},
                                properties = DialogProperties(
                                    dismissOnBackPress = false,
                                    dismissOnClickOutside = false
                                )
                            ) {
                                CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                        if (showTicketDialog) {
                            TicketDialog(
                                onDismiss = { showTicketDialog = false },
                                uid = currentUser!!.uid,
                                tickets = user.tickets,
                                paddingValues = innerPadding
                            )
                        }
                        Content(
                            pv = innerPadding,
                            navController = navController,
                            currentUser = currentUser!!,
                            realNameValue = realName,
                            userNameValue = userName,
                            userNameIsEmpty = userNameIsEmpty,
                            userNameAlreadyUsed = userNameAlreadyUsed,
                            emailValue = user.email,
                            phoneNumberValue = phoneNumberName,
                            gender = gender,
                            birthDayValue = user.birthDate,
                            changeRealName = {
                                realName = it
                                realNameChanged = true
                            },
                            changeUserName = {
                                userName = it
                                userNameChanged = true
                                userNameIsEmpty = false
                                userNameAlreadyUsed = false
                            },
                            changePhoneNumber = {
                                phoneNumberName = it
                                phoneNumberChanged = true
                            },
                            changeGender = {
                                gender = it
                                genderChanged = true
                            },
                            changeBirthDay = {
                                birthDay = it
                                birthDayChanged = true
                            },
                            viewModel = viewModel,
                            showSnackbar = { scope.launch { snackbarHostState.showSnackbar(it) } },
                            deleteAccount = { viewModel.deleteAccount(currentUser!!, it) },
                            snackbarHostState = snackbarHostState
                        )
                    }

                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Content(
    pv: PaddingValues,
    navController: NavHostController,
    currentUser: FirebaseUser,
    viewModel: ProfileViewModel,
    realNameValue: String,
    userNameValue: String,
    userNameIsEmpty: Boolean,
    userNameAlreadyUsed: Boolean,
    emailValue: String,
    snackbarHostState: SnackbarHostState,
    phoneNumberValue: String,
    gender: String?,
    birthDayValue: Timestamp?,
    changeRealName: (String) -> Unit,
    changeUserName: (String) -> Unit,
    changePhoneNumber: (String) -> Unit,
    changeGender: (String) -> Unit,
    changeBirthDay: (Timestamp) -> Unit,
    showSnackbar: (String) -> Unit,
    deleteAccount: (String) -> Unit
) {
    val context = LocalContext.current
    var profilePictureClicked by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        yearRange = 1920..LocalDate.now().year - 15,
        initialSelectedDateMillis = birthDayValue?.toDate()?.time
    )
    val selectedDate = datePickerState.selectedDateMillis?.let {
        changeBirthDay(TimeFunctions().convertDatePickerTimeToTimestamp(it))
        TimeFunctions().convertTimestampToDate(Timestamp(Date(it)))
    } ?: ""

    var profilePictureUri by remember {
        mutableStateOf(currentUser.photoUrl)
    }
    var verificationLinkSent by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    var profilePictureIsLoading by remember { mutableStateOf(false) }
    var userHasPicture by remember { mutableStateOf(currentUser.photoUrl != null) }

    LaunchedEffect(key1 = viewModel.sendEmailVerificationState.value) {
        when (val resource = viewModel.sendEmailVerificationState.value) {
            is Resource.Error -> {
                showSnackbar(resource.messageResource?.let { context.getString(it) } ?: "")
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {}
            is Resource.Success -> {
                verificationLinkSent = true
            }
        }
    }

    LaunchedEffect(key1 = viewModel.uploadPictureState.value) {
        when (val resource = viewModel.uploadPictureState.value) {
            is Resource.Error -> {
                profilePictureIsLoading = false
                showSnackbar(resource.messageResource?.let { context.getString(it) } ?: "")
            }

            is Resource.Idle -> {
                profilePictureIsLoading = false
            }

            is Resource.Loading -> {
                profilePictureIsLoading = true
            }

            is Resource.Success -> {
                profilePictureIsLoading = false
                profilePictureUri = currentUser.photoUrl
                userHasPicture = profilePictureUri != null
            }
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .padding(top = pv.calculateTopPadding())
    ) {
        if (showDeleteAccountDialog) {
            var password by remember { mutableStateOf("") }
            AlertDialog(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = stringResource(R.string.are_you_sure_you_want_to_delete_your_account)
                    )
                },
                title = {
                    Text(stringResource(R.string.are_you_sure_you_want_to_delete_your_account))
                },
                text = {
                    Column {
                        Text(stringResource(R.string.delete_account_dialog_text))
                        TextField(
                            value = password,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            onValueChange = {password = it},
                            label = {
                                Text(stringResource(R.string.password)+":")
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }

                },
                onDismissRequest = { showDeleteAccountDialog = false },
                dismissButton = {
                    Button(onClick = { showDeleteAccountDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = password.isNotBlank(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        onClick = {
                            deleteAccount(password)
                            showDeleteAccountDialog = false
                        }
                    ) {
                        Text(text = stringResource(R.string.delete_account))
                    }
                })
        }

        if (verificationLinkSent) {
            AlertDialog(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "success"
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.please_check_your_email)
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.sending_verification_link_dialog_text)
                    )
                },
                onDismissRequest = { verificationLinkSent = false },
                confirmButton = {
                    Button(onClick = { verificationLinkSent = false }) {
                        Text(text = stringResource(R.string.okay))
                    }
                },
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {}
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false
                )
            }
        }
        if (profilePictureClicked) {
            BottomSheet(
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            profilePictureClicked = false
                        }
                    }
                },
                state = sheetState,
                userHasPicture = userHasPicture,
                currentUser = currentUser,
                viewModel = viewModel,
                scope = scope,
                snackbarHostState = snackbarHostState,
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        profilePictureClicked = true
                    }
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddAPhoto,
                    contentDescription = "change photo",
                    modifier = Modifier
                        .size(IconButtonDefaults.largeIconSize)
                        .align(Alignment.Center)
                        .background(Color.White.copy(alpha = 0.5f), CircleShape)
                        .padding(5.dp)
                        .zIndex(2f)
                )
                if (profilePictureIsLoading) {
                    Box(modifier = Modifier.align(Alignment.Center)) {
                        CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    if (profilePictureUri != null) {
                        GlideImage(
                            imageModel = { currentUser.photoUrl },
                            requestBuilder = {
                                val thumbnailRequest = Glide
                                    .with(context)
                                    .asBitmap()
                                    .load(currentUser.photoUrl)
                                    .apply(RequestOptions().override(100))

                                Glide
                                    .with(context)
                                    .asBitmap()
                                    .apply(
                                        RequestOptions()
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    )
                                    .thumbnail(thumbnailRequest)
                                    .transition(withCrossFade())
                            },
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        Image(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape)
                                .background(
                                    Color.LightGray,
                                    CircleShape
                                )
                        )
                    }
                }
            }



            Spacer(modifier = Modifier.height(15.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(0.9f),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AccountInformationRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "user name"
                            )
                        },
                        label = stringResource(R.string.real_name_optional),
                        value = realNameValue
                    ) {
                        changeRealName(it)
                    }
                    HorizontalDivider()
                    AccountInformationRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.PersonOutline,
                                contentDescription = "user name"
                            )
                        },
                        label = stringResource(R.string.username),
                        userNameIsEmpty = userNameIsEmpty,
                        userNameAlreadyUsed = userNameAlreadyUsed,
                        value = userNameValue
                    ) {
                        changeUserName(it)
                    }

                }

            }
            Spacer(modifier = Modifier.height(15.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(0.9f),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AccountEmailInformationRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = "user name"
                            )
                        },
                        value = emailValue,
                        clickedEditForEmail = {
                            if (!currentUser.providerData.map { it.providerId }
                                    .contains(GoogleAuthProvider.PROVIDER_ID)) {
                                navController.navigate("reset_email")
                            } else {
                                showSnackbar(context.getString(R.string.cannot_change_email_because_of_signing_in_with_google))
                            }
                        }
                    )
                    HorizontalDivider()
                    AccountInformationRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = "user name"
                            )
                        },
                        label = stringResource(R.string.phone_number_optional),
                        value = phoneNumberValue
                    ) {
                        changePhoneNumber(it)
                    }
                }

            }
            Spacer(modifier = Modifier.height(15.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(0.9f),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AccountGenderInformationRow(
                        value = gender ?: Gender.PreferNotToSay.value,
                        options = listOf(
                            Gender.PreferNotToSay.value,
                            Gender.Male.value,
                            Gender.Female.value
                        ),
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Transgender,
                                contentDescription = "Gender"
                            )
                        },
                    ) {
                        changeGender(it)
                    }
                    HorizontalDivider()
                    AccountDateInformationRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = "birth day"
                            )
                        },
                        value = selectedDate
                    ) { showDatePicker = true }
                }

            }
            Spacer(modifier = Modifier.height(15.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                if (!currentUser.providerData.map { it.providerId }
                        .contains(GoogleAuthProvider.PROVIDER_ID)) {
                    ChangePasswordButton { navController.navigate("reset_password") }
                }
                TextButton(
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = { showDeleteAccountDialog = true }
                ) {
                    Text(text = stringResource(R.string.delete_account))
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
private fun BottomSheet(
    userHasPicture: Boolean,
    onDismiss: () -> Unit,
    state: SheetState,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    currentUser: FirebaseUser,
    viewModel: ProfileViewModel
) {
    val context = LocalContext.current
    val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState =
            rememberPermissionState(permission = Manifest.permission.READ_MEDIA_IMAGES)
        permissionState

    } else {
        val permissionState =
            rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionState
    }
    val mediaIsGranted = remember {
        mutableStateOf(
            permissionState.status.isGranted
        )
    }

    val selectedImageUri = remember {
        mutableStateOf<Uri?>(null)

    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri.value = uri
            viewModel.updateProfilePicture(
                pictureUri = selectedImageUri.value!!,
                myUid = currentUser.uid
            )
            onDismiss()
        }
    }
    val haslaunched = remember { mutableStateOf(false) }
    LaunchedEffect(key1 = permissionState.status.isGranted) {
        mediaIsGranted.value = permissionState.status.isGranted
        if (haslaunched.value) {
            if (mediaIsGranted.value) {
                galleryLauncher.launch("image/*")
            }
        } else {
            haslaunched.value = true
        }
    }
    ModalBottomSheet(
        shape = RoundedCornerShape(10.dp),
        dragHandle = null,
        onDismissRequest = onDismiss,
        sheetState = state
    ) {
        Spacer(modifier = Modifier.height(5.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (userHasPicture) {
                OutlinedButton(
                    onClick = {
                        if (mediaIsGranted.value) {
                            galleryLauncher.launch("image/*")
                        } else {
                            if (!permissionState.status.shouldShowRationale) {

                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.media_permission_rationale),
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Long,
                                        actionLabel = context.getString(R.string.permission_settings)
                                    )
                                    if (SnackbarResult.ActionPerformed == result){
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            }else {
                                permissionState.launchPermissionRequest()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = stringResource(R.string.change_profile_picture))
                }
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "delete",
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(
                        onClick = {
                            viewModel.updateProfilePicture(
                                pictureUri = null,
                                myUid = currentUser.uid
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Red
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = stringResource(R.string.remove_profile_picture))
                    }
                }

            } else {
                OutlinedButton(
                    onClick = {
                        if (mediaIsGranted.value) {
                            galleryLauncher.launch("image/*")
                        } else {
                            if (!permissionState.status.shouldShowRationale) {

                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.media_permission_rationale),
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Long,
                                        actionLabel = context.getString(R.string.permission_settings)
                                    )
                                    if (SnackbarResult.ActionPerformed == result){
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            }else {
                                permissionState.launchPermissionRequest()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = stringResource(R.string.upload_profile_picture))
                }
            }

        }

    }
}

@Composable
private fun ChangePasswordButton(onCliCk: () -> Unit) {
    OutlinedButton(onClick = onCliCk, shape = RoundedCornerShape(10.dp)) {
        Text(text = stringResource(R.string.change_password), fontSize = 13.sp)
    }
}

@Composable
private fun AccountDateInformationRow(
    icon: @Composable () -> Unit = {},
    value: String,
    onClick: () -> Unit
) {
    TextField(
        enabled = false,
        trailingIcon = icon,
        label = { Text(text = stringResource(R.string.birth_date)) },
        value = value,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(7.dp),
        colors = TextFieldDefaults.colors(
            disabledContainerColor = Color.Transparent,
            disabledTextColor = TextFieldDefaults.colors().unfocusedTextColor,
            disabledLabelColor = TextFieldDefaults.colors().unfocusedLabelColor,
            disabledTrailingIconColor = TextFieldDefaults.colors().unfocusedTrailingIconColor,
            disabledIndicatorColor = Color.Transparent
        )
    )


}

@Composable
private fun AccountGenderInformationRow(
    value: String,
    options: List<String>,
    icon: @Composable () -> Unit = {},
    onValueChange: (String) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    Column {
        TextField(
            enabled = false,
            trailingIcon = icon,
            label = { Text(text = stringResource(R.string.gender)) },
            value = stringResource(getGenderDisplayFromValue(value)),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded.value = true }
                .padding(7.dp),
            colors = TextFieldDefaults.colors(
                disabledContainerColor = Color.Transparent,
                disabledTextColor = TextFieldDefaults.colors().unfocusedTextColor,
                disabledLabelColor = TextFieldDefaults.colors().unfocusedLabelColor,
                disabledTrailingIconColor = TextFieldDefaults.colors().unfocusedTrailingIconColor,
                disabledIndicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        DropdownMenu(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(10.dp),
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(text = stringResource(getGenderDisplayFromValue(it))) },
                    onClick = {
                        onValueChange(it)
                        expanded.value = false
                    })
            }
        }
    }

}

@Composable
private fun AccountEmailInformationRow(
    icon: @Composable () -> Unit = {},
    value: String,
    clickedEditForEmail: () -> Unit,
) {
    TextField(
        enabled = false,
        trailingIcon = icon,
        label = { Text(text = stringResource(R.string.email_address)) },
        value = value,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp)
            .clickable { clickedEditForEmail.invoke() },
        colors = TextFieldDefaults.colors(
            disabledTextColor = TextFieldDefaults.colors().unfocusedTextColor,
            disabledContainerColor = Color.Transparent,
            disabledLabelColor = TextFieldDefaults.colors().unfocusedLabelColor,
            disabledTrailingIconColor = TextFieldDefaults.colors().unfocusedTrailingIconColor,
            disabledLeadingIconColor = TextFieldDefaults.colors().unfocusedLeadingIconColor,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun AccountInformationRow(
    icon: @Composable () -> Unit = {},
    label: String,
    value: String,
    userNameIsEmpty: Boolean = false,
    userNameAlreadyUsed: Boolean = false,
    onValueChange: (String) -> Unit,
) {
    TextField(
        trailingIcon = icon,
        isError = userNameAlreadyUsed || userNameIsEmpty,
        label = { Text(text = label) },
        value = value,
        onValueChange = onValueChange,
        supportingText = if (userNameIsEmpty) {
            {
                Text(stringResource(R.string.username_cannot_be_empty))
            }
        } else if (userNameAlreadyUsed) {
            {
                Text(stringResource(R.string.username_already_used))
            }
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp),
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            errorContainerColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun ProfilePreview() {
    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(Color.Black)
            .clickable {}
    ) {
        Icon(
            imageVector = Icons.Outlined.AddAPhoto,
            contentDescription = "change photo",
            modifier = Modifier
                .size(IconButtonDefaults.smallIconSize)
                .align(Alignment.Center)
                .background(Color.White.copy(alpha = 0.5f), CircleShape)
                .padding(5.dp)
                .zIndex(2f)
        )
    }
}