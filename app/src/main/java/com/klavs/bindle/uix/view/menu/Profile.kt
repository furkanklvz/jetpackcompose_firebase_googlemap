package com.klavs.bindle.uix.view.menu

import android.Manifest
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Transgender
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.view.auth.convertMillisToDate
import com.klavs.bindle.uix.view.loading.LoadingAnimation
import com.klavs.bindle.uix.viewmodel.ProfileViewModel
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = true) {
        viewModel.getUserInfos()
    }
    DisposableEffect(true) {
        onDispose {
            viewModel.currentUserJob?.cancel()
        }
    }
    val currentUser by viewModel.currentUser.collectAsState()

    var isLoading by remember { mutableStateOf(false) }

    var realName by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var phoneNumberName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthDay by remember { mutableLongStateOf(0L) }
    var realNameChanged by remember { mutableStateOf(false) }
    var userNameChanged by remember { mutableStateOf(false) }
    var phoneNumberChanged by remember { mutableStateOf(false) }
    var genderChanged by remember { mutableStateOf(false) }
    var birthDayChanged by remember { mutableStateOf(false) }
    var userNameIsEmpty by remember { mutableStateOf(false) }
    var userNameAlreadyUsed by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.popBackStack()
        }
    }
    LaunchedEffect(viewModel.checkUniqueUsernameState.value) {
        when (val state = viewModel.checkUniqueUsernameState.value) {
            is Resource.Error -> {
                isLoading = false
                Toast.makeText(navController.context, state.message!!, Toast.LENGTH_SHORT).show()
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isLoading = true
            }

            is Resource.Success -> {
                isLoading = false
                if (state.data!!) {
                    val newUserData = hashMapOf<String, Any?>(
                        "userName" to userName
                    )
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
                    viewModel.updateUserData(
                        newUserData = newUserData
                    )
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
                Toast.makeText(navController.context, resource.message!!, Toast.LENGTH_SHORT).show()
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isLoading = true
            }

            is Resource.Success -> {
                isLoading = false
                Toast.makeText(navController.context, "Updated Successfully", Toast.LENGTH_SHORT)
                    .show()
                navController.popBackStack()
            }
        }
    }
    if (isLoading) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .zIndex(2f)
            .clickable(enabled = false) {}) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                actions = {
                    TextButton(onClick = {
                        if (userName.isEmpty()) {
                            userNameIsEmpty = true
                        } else {
                            if (userNameChanged) {
                                viewModel.checkUniqueUsername(
                                    userName = userName,
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
                                viewModel.updateUserData(
                                    newUserData = newUserData
                                )
                            }
                        }
                    }) {
                        Text(text = "Save", fontSize = 18.sp)
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
                title = { Text(text = "Profile") })
        }
    ) { innerPadding ->
        when (val resource = viewModel.userDataState.value) {
            is Resource.Error -> {
                navController.popBackStack()

                Toast.makeText(navController.context, resource.message, Toast.LENGTH_SHORT).show()

            }

            is Resource.Idle -> {}
            is Resource.Loading -> {}

            is Resource.Success -> {
                val user = resource.data!!
                LaunchedEffect(key1 = true) {
                    realName = user.realName
                    userName = user.userName
                    phoneNumberName = user.phoneNumber
                    gender = user.gender
                    birthDay = user.birthDate!!
                }
                Content(
                    pv = innerPadding,
                    navController = navController,
                    currentUser = currentUser,
                    realNameValue = realName,
                    userNameValue = userName,
                    userNameIsEmpty = userNameIsEmpty,
                    userNameAlreadyUsed = userNameAlreadyUsed,
                    emailValue = user.email,
                    phoneNumberValue = phoneNumberName,
                    genderValue = gender,
                    birthDayValue = user.birthDate!!,
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
                    isVerified = currentUser?.isEmailVerified ?: false
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    pv: PaddingValues,
    navController: NavHostController,
    currentUser: FirebaseUser?,
    viewModel: ProfileViewModel = hiltViewModel(),
    realNameValue: String,
    userNameValue: String,
    userNameIsEmpty: Boolean,
    userNameAlreadyUsed: Boolean,
    emailValue: String,
    phoneNumberValue: String,
    genderValue: String,
    birthDayValue: Long,
    changeRealName: (String) -> Unit,
    changeUserName: (String) -> Unit,
    changePhoneNumber: (String) -> Unit,
    changeGender: (String) -> Unit,
    changeBirthDay: (Long) -> Unit,
    isVerified: Boolean
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    var profilePictureClicked by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        yearRange = 1920..LocalDate.now().year - 15,
        initialSelectedDateMillis = birthDayValue
    )

    var profilePictureUri by remember {
        mutableStateOf(currentUser!!.photoUrl)
    }
    var verificationLinkSent by remember { mutableStateOf(false) }

    var profilePictureIsLoading by remember { mutableStateOf(false) }
    var userHasPicture by remember { mutableStateOf(currentUser!!.photoUrl != null) }
    val selectedDate = datePickerState.selectedDateMillis?.let {
        changeBirthDay(it)
        convertMillisToDate(it)
    } ?: ""
    LaunchedEffect(key1 = viewModel.sendEmailVerificationState.value) {
        when (val resource = viewModel.sendEmailVerificationState.value) {
            is Resource.Error -> {
                Toast.makeText(navController.context, resource.message!!, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(navController.context, resource.message!!, Toast.LENGTH_SHORT).show()
            }

            is Resource.Idle -> {
                profilePictureIsLoading = false
            }

            is Resource.Loading -> {
                profilePictureIsLoading = true
            }

            is Resource.Success -> {
                profilePictureIsLoading = false
                profilePictureUri = currentUser!!.photoUrl
                userHasPicture = profilePictureUri != null
            }
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .padding(top = pv.calculateTopPadding())
    ) {
        if (verificationLinkSent) {
            AlertDialog(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "succes"
                    )
                },
                title = { Text(text = "Check your e-mail") },
                text = { Text(text = "We send a verification link to your e-mail. Please check your e-mail and verify your account") },
                onDismissRequest = { verificationLinkSent = false },
                confirmButton = {
                    Button(onClick = { verificationLinkSent = false }) {
                        Text(text = "Ok")
                    }
                },
            )
        }

        if (showDatePicker) {
            Popup(
                onDismissRequest = { showDatePicker = false },
                alignment = Alignment.Center,
                properties = PopupProperties(
                    dismissOnBackPress = true
                )
            ) {
                Box(
                    modifier = Modifier
                        .shadow(elevation = 4.dp)
                        .background(Color.Transparent)
                        .zIndex(1f)
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
                }
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
                userHasPicture = userHasPicture
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
                    .size(screenHeight / 8)
                    .clip(CircleShape)
                    .clickable {
                        profilePictureClicked = true
                    }
            ) {
                if (profilePictureIsLoading) {
                    Box(modifier = Modifier.align(Alignment.Center)) {
                        LoadingAnimation(screenHeight / 10)
                    }
                } else {
                    if (profilePictureUri != null) {
                        GlideImage(
                            imageModel = { currentUser!!.photoUrl },
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.default_profile),
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
            Spacer(modifier = Modifier.height(7.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isVerified) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Verified Account",
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Rounded.CheckCircleOutline,
                            contentDescription = "verified"
                        )
                    }
                } else {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Unverified Account",
                            modifier = Modifier.padding(10.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (viewModel.sendEmailVerificationState.value !is Resource.Success) {
                        TextButton(onClick = { viewModel.sendEmailVerification() }) {
                            Text(text = "Verify Now")
                        }
                    } else {
                        TextButton(enabled = false, onClick = {}) {
                            Text(text = "Check your e-mail")
                        }
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
                        label = "Real name (Optional)",
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
                        label = "User name",
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
                            navController.navigate("reset_email")
                        }
                    )
                    HorizontalDivider()
                    AccountInformationRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = "user name"
                            )
                        }, label = "Phone Number (Optional)",
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
                        value = genderValue,
                        options = listOf("Male", "Female", "Other", "Prefer not to say"),
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
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                ChangePasswordButton { navController.navigate("reset_password") }
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
    viewModel: ProfileViewModel = hiltViewModel()
) {
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

    val selectedImageUri = rememberSaveable {
        mutableStateOf<Uri?>(null)

    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri.value = uri
            viewModel.updateProfilePicture(pictureUri = selectedImageUri.value!!)
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
                            permissionState.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Change profile picture")
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
                            viewModel.updateProfilePicture(pictureUri = null)
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Red
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Remove profile picture")
                    }
                }

            } else {
                OutlinedButton(
                    onClick = {
                        if (mediaIsGranted.value) {
                            galleryLauncher.launch("image/*")
                        } else {
                            permissionState.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Upload a profile picture")
                }
            }

        }

    }
}

@Composable
private fun ChangePasswordButton(onCliCk: () -> Unit) {
    OutlinedButton(onClick = onCliCk, shape = RoundedCornerShape(10.dp)) {
        Text(text = "Change Password", fontSize = 13.sp)
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
        label = { Text(text = "Birth Date") },
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
            label = { Text(text = "Gender") },
            value = value,
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
                    text = { Text(text = it) },
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
        label = { Text(text = "E-mail Address") },
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
                Text("Username can't be empty")
            }
        } else if (userNameAlreadyUsed) {
            {
                Text("This username is already used")
            }
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp),
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        )
    )
}

@Preview
@Composable
fun ProfilePreview() {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clickable { }
            .shadow(5.dp, CircleShape)
    ) {
        Image(
            painter = painterResource(id = R.drawable.default_profile),
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
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = "add",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .zIndex(1f)
                .size(50.dp)
        )

    }
}