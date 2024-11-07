package com.klavs.bindle.uix.view.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.AuthResult
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.BottomNavItem
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.ui.theme.logoFont
import com.klavs.bindle.uix.view.loading.LoadingAnimation
import com.klavs.bindle.uix.viewmodel.LogInViewModel

@Composable
fun LogIn(navController: NavHostController, viewModel: LogInViewModel = hiltViewModel()) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    val email = remember { mutableStateOf("") }
    val emailForReset = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val resetPasswordIsEnable = remember { mutableStateOf(false) }
    val isLoading = remember {
        mutableStateOf(false)
    }
    val isError = remember {
        mutableStateOf(false)
    }
    val errorMessage = remember {
        mutableStateOf("")
    }
    val emailError = remember {
        mutableStateOf(false)
    }
    val passwordError = remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = viewModel.logInState.value) {
        when (val result = viewModel.logInState.value) {
            is Resource.Error -> {
                errorMessage.value = result.message!!
                isError.value = true
                isLoading.value = false
            }

            is Resource.Loading -> {
                isLoading.value = true
            }

            is Resource.Idle -> {}
            is Resource.Success -> {
                isLoading.value = false
                navController.navigate(BottomNavItem.Home.route) {
                    popUpTo(navController.currentDestination?.route ?: "") {
                        inclusive = true
                    }
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(
                top = WindowInsets.statusBars
                    .asPaddingValues()
                    .calculateTopPadding()
            )
    ) {
        if (isLoading.value){
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .zIndex(1f)
                .clickable(enabled = false) {}) {
                Box(modifier = Modifier.align(Alignment.Center)) {
                    LoadingAnimation(size = 250.dp)
                }

            }
        }
        if (isError.value) {
            AlertDialog(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = "error"
                    )
                },
                title = { Text(text = "Error") },
                text = { Text(text = errorMessage.value) },
                onDismissRequest = { isError.value = false },
                confirmButton = {
                    Button(onClick = { isError.value = false }) {
                        Text(text = "Ok")
                    }
                },
            )
        }
        if (resetPasswordIsEnable.value) {
            AlertDialog(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.LockReset,
                        contentDescription = "reset password"
                    )
                },
                title = { Text(text = "Reset password") },
                text = {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Enter your email to reset your password")
                        Spacer(modifier = Modifier.padding(screenHeight / 100))
                        OutlinedTextField(
                            placeholder = { Text(text = "Your e-mail address") },
                            value = emailForReset.value,
                            onValueChange = { emailForReset.value = it },
                            shape = CircleShape
                        )
                    }

                },
                onDismissRequest = { resetPasswordIsEnable.value = false },
                confirmButton = {
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "Send link to reset the password")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { resetPasswordIsEnable.value = false }) {
                        Text(text = "Cancel")
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
                    .width(screenWith / 1.3f)

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
                            .size(screenHeight / 20),
                        contentScale = ContentScale.FillHeight
                    )
                    Spacer(modifier = Modifier.padding(screenHeight / 100))
                    Text(
                        text = "Bindle",
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
                        hasError = emailError.value,
                        value = email.value,
                        onValueChange = {
                            email.value = it
                            emailError.value = false
                        },
                        label = "E-Mail",
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.rounded_alternate_email_24),
                                "email"
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    PasswordTextField(
                        hasError = passwordError.value,
                        value = password.value,
                        onValueChange = {
                            password.value = it
                            passwordError.value = false
                        },
                        label = "Password",
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
                    TextButton(onClick = { resetPasswordIsEnable.value = true }) {
                        Text(text = "Forgot password?")
                    }
                }
                Column {
                    LogInButton(text = "Log in") {
                        if (viewModel.logInState.value != Resource.Loading<AuthResult>()) {
                            if (email.value.isEmpty()) {
                                emailError.value = true
                            }
                            if (password.value.isEmpty()) {
                                passwordError.value = true
                            }
                            if (!emailError.value && !passwordError.value) {
                                viewModel.logIn(email.value, password.value)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.padding(vertical = 5.dp))
                    CreateAccountButton(text = "Create an account") { navController.navigate("create_user") }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    Text(
                        text = "or",
                        fontSize = 15.sp,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                GoogleSubmitButton(text = "Continue with Google") {
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
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    OutlinedTextField(
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        isError = hasError,
        modifier = Modifier
            .width(screenWith / 1.2f),
        shape = RoundedCornerShape(10.dp),
        leadingIcon = icon,
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(text = label, fontSize = 14.sp) },
        textStyle = TextStyle(fontSize = 14.sp)
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
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val passwordVisible = remember { mutableStateOf(false) }
    OutlinedTextField(
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier
            .width(screenWith / 1.2f),
        shape = RoundedCornerShape(10.dp),
        leadingIcon = icon,
        isError = hasError,
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(text = label, fontSize = 14.sp) },
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
        },
        textStyle = TextStyle(fontSize = 14.sp)
    )
}

@Composable
fun LogInButton(text: String, onClick: () -> Unit) {
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(screenWith / 1.2f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun CreateAccountButton(text: String, onClick: () -> Unit) {
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    OutlinedButton(
        colors = ButtonDefaults.elevatedButtonColors(
        ),
        onClick = onClick,
        modifier = Modifier
            .width(screenWith / 1.2f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun GoogleSubmitButton(text: String, onClick: () -> Unit) {
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(screenWith / 1.2f),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Text(text = text)
            }
            Icon(
                painter = painterResource(id = R.drawable.google_icon),
                contentDescription = "google",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .height(20.dp),
                tint = Color.Unspecified
            )
        }

    }
}

@Preview
@Composable
fun LogInPreview() {
    LogIn(navController = rememberNavController())
}