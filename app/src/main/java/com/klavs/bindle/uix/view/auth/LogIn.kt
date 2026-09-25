package com.klavs.bindle.uix.view.auth

import android.credentials.GetCredentialException
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.AuthResult
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.sealedclasses.BottomNavItem
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.logoFont
import com.klavs.bindle.uix.viewmodel.LogInViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LogIn(navController: NavHostController, viewModel: LogInViewModel) {
    val context = LocalContext.current

    var isLoading by remember {
        mutableStateOf(false)
    }
    var isError by remember {
        mutableStateOf(false)
    }
    var errorMessage by remember {
        mutableStateOf("")
    }

    var emailSent by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.signInWithGoogleState.value) {
        when (val resource = viewModel.signInWithGoogleState.value) {
            is Resource.Error -> {
                isLoading = false
                isError = true
                errorMessage = resource.messageResource?.let { context.getString(it)}?:""
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isLoading = true
            }

            is Resource.Success -> {
                isLoading = false
                if (resource.data!!.additionalUserInfo!!.isNewUser) {
                    navController.navigate("greeting") {
                        popUpTo(navController.currentDestination?.route ?: "") {
                            inclusive = true
                        }
                    }
                } else {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo(navController.currentDestination?.route ?: "") {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(key1 = viewModel.logInState.value) {
        when (val result = viewModel.logInState.value) {
            is Resource.Error -> {
                errorMessage = result.messageResource?.let { context.getString(it)}?:""
                isError = true
                isLoading = false
            }

            is Resource.Loading -> {
                isLoading = true
            }

            is Resource.Idle -> {}
            is Resource.Success -> {
                isLoading = false
                navController.navigate(BottomNavItem.Home.route) {
                    popUpTo(navController.currentDestination?.route ?: "") {
                        inclusive = true
                    }
                }
            }
        }
    }
    LaunchedEffect(viewModel.resetPasswordEmailState.value) {
        emailSent = when (viewModel.resetPasswordEmailState.value) {
            is Resource.Success -> {
                true
            }
            else -> {
                false
            }
        }
    }
    LoginContent(
        isLoading = isLoading,
        isError = isError,
        errorMessage = errorMessage,
        resetError = { isError = false },
        resetEmailSent = { emailSent = false },
        emailSent = emailSent,
        sendPasswordResetEmail = { viewModel.sendPasswordResetEmail(it) },
        logInState = viewModel.logInState.value,
        logIn = {
            viewModel.logIn(it.first, it.second)
        },
        navigateToCreatUser = { navController.navigate("create_user") },
        signInWithGoogle = {viewModel.onSignInWithGoogle(it)}
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoginContent(
    isLoading: Boolean,
    isError: Boolean,
    errorMessage: String,
    resetError :() -> Unit,
    resetEmailSent :() -> Unit,
    emailSent :Boolean,
    sendPasswordResetEmail :(String)->Unit,
    logInState: Resource<AuthResult>,
    logIn: (Pair<String,String>) -> Unit,
    navigateToCreatUser :() -> Unit,
    signInWithGoogle : (Credential) -> Unit
){
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var emailForReset by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var resetPasswordIsEnable by remember { mutableStateOf(false) }
    var emailError by remember {
        mutableStateOf(false)
    }
    var passwordError by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding()
                )
        ) {
            if (isLoading) {
                Dialog(
                    onDismissRequest = {},
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            if (isError) {
                AlertDialog(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = "error"
                        )
                    },
                    title = { Text(text = stringResource(R.string.error)) },
                    text = { Text(text = errorMessage) },
                    onDismissRequest = resetError,
                    confirmButton = {
                        Button(onClick = resetError) {
                            Text(text = stringResource(R.string.okay))
                        }
                    },
                )
            }
            if (resetPasswordIsEnable) {

                AlertDialog(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.LockReset,
                            contentDescription = "reset password"
                        )
                    },
                    title = { Text(text = stringResource(R.string.reset_password)) },
                    text = {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = stringResource(R.string.enter_email_to_reset_password))
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                placeholder = { Text(text = stringResource(R.string.your_email_address)) },
                                value = emailForReset,
                                onValueChange = {
                                    resetEmailSent()
                                    emailForReset = it
                                },
                                shape = CircleShape
                            )
                        }

                    },
                    onDismissRequest = { resetPasswordIsEnable = false },
                    confirmButton = {
                        Button(enabled = !emailSent,
                            onClick = {sendPasswordResetEmail(emailForReset)}) {
                            Text(text = if (emailSent) stringResource(R.string.please_check_your_email) else stringResource(R.string.send_link_to_reset_password))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { resetPasswordIsEnable = false }) {
                            Text(text = stringResource(R.string.cancel))
                        }
                    })
            }
            Column(
                Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(36.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)

                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_no_background),
                            contentDescription = "near me",
                            modifier = Modifier
                                .size(50.dp),
                            contentScale = ContentScale.FillHeight
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            fontFamily = logoFont,
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize
                        )
                    }

                }
                Spacer(modifier = Modifier.height(36.dp))


                Column(
                    Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column {
                        LogInTextField(
                            hasError = emailError,
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = false
                            },
                            label = stringResource(R.string.email),
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    "email"
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        PasswordTextField(
                            hasError = passwordError,
                            value = password,
                            onValueChange = {
                                password = it
                                passwordError = false
                            },
                            label = stringResource(R.string.password),
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.key),
                                    "password"
                                )
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { resetPasswordIsEnable = true }) {
                            Text(text = stringResource(R.string.forgot_password))
                        }
                    }
                    Column {
                        LogInButton(text = stringResource(R.string.sign_in)) {
                            if (logInState != Resource.Loading<AuthResult>()) {
                                if (email.isBlank()) {
                                    emailError = true
                                }
                                if (password.isBlank()) {
                                    passwordError = true
                                }
                                if (!emailError && !passwordError) {
                                    logIn(email to password)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.padding(vertical = 5.dp))
                        CreateAccountButton(text = stringResource(R.string.create_account)) { navigateToCreatUser() }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.or),
                            fontSize = 15.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    SignInWithGoogleButton(
                        text = stringResource(R.string.continue_with_google),
                        onError = {scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.no_google_accont_found)) }}
                    ) { credential ->
                        signInWithGoogle(credential)
                    }

                }
            }
        }
    }
}

@Composable
private fun LogInTextField(
    hasError: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: @Composable () -> Unit
) {
    OutlinedTextField(
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        isError = hasError,
        modifier = Modifier
            .fillMaxWidth(0.8f),
        shape = RoundedCornerShape(10.dp),
        leadingIcon = icon,
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(text = label) }
    )

}

@Composable
private fun PasswordTextField(
    hasError: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: @Composable () -> Unit
) {
    val passwordVisible = remember { mutableStateOf(false) }
    OutlinedTextField(
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth(0.8f),
        shape = RoundedCornerShape(10.dp),
        leadingIcon = icon,
        isError = hasError,
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(text = label) },
        visualTransformation = if (passwordVisible.value) VisualTransformation.None
        else PasswordVisualTransformation(),
        trailingIcon = {
            Icon(
                painter = painterResource(
                    id = if (passwordVisible.value) R.drawable.rounded_visibility_24
                    else R.drawable.rounded_visibility_off_24
                ), contentDescription = "visibility",
                modifier = Modifier.clickable {
                    passwordVisible.value = !passwordVisible.value
                }
            )
        }
    )
}

@Composable
fun LogInButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun CreateAccountButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        colors = ButtonDefaults.elevatedButtonColors(
        ),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun SignInWithGoogleButton(text: String,onError :() ->Unit, onGetCredentialResponse: (Credential) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    OutlinedButton(
        onClick = {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            coroutineScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = context
                    )
                    onGetCredentialResponse(result.credential)
                } catch (e: Exception) {
                    if (e.message == GetCredentialException.TYPE_NO_CREDENTIAL) {
                        onError()
                    }
                }
            }
        },
        modifier = Modifier
            .width(screenWith / 1.2f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(text = text, modifier = Modifier.align(Alignment.Center))

            Icon(
                painter = painterResource(id = R.drawable.google_icon),
                contentDescription = "google",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(24.dp),
                tint = Color.Unspecified
            )
        }

    }
}

@Preview
@Composable
fun LogInPreview() {
    LoginContent(
        isLoading = false,
        isError = false,
        errorMessage = "",
        resetError = {},
        resetEmailSent = {  },
        emailSent = true,
        sendPasswordResetEmail = {},
        logInState = Resource.Idle(),
        logIn = {  },
        navigateToCreatUser = {},
        signInWithGoogle = {}
    )
}