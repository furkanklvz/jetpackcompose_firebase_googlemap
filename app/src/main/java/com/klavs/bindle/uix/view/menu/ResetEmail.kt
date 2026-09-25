package com.klavs.bindle.uix.view.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.klavs.bindle.R
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResetEmail(
    navController: NavHostController,
    myUid: String?,
    viewModel: ProfileViewModel
) {
    val context = LocalContext.current

    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val passwordIsEmpty = remember { mutableStateOf(false) }
    val emailIsEmpty = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = viewModel.updateEmailState.value) {
        when (val resource = viewModel.updateEmailState.value) {
            is Resource.Error -> {
                isLoading.value = false
                snackbarHostState.showSnackbar(resource.messageResource?.let { context.getString(it) }
                    ?: "")
            }

            is Resource.Idle -> {}
            is Resource.Loading -> {
                isLoading.value = true
            }

            is Resource.Success -> {
                isLoading.value = false
                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.we_sent_a_verification_link)) }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {navController.popBackStack()}
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "turn back")
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.change_email_address)
                    )
                })
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (isLoading.value) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .clickable(false) {}) {
                    Box(modifier = Modifier.align(Alignment.Center)) {
                        CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
            Column(
                Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 15.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PasswordTextField(
                    isEmpty = passwordIsEmpty.value,
                    value = password.value
                ) {
                    password.value = it
                    if (password.value.isBlank()) {
                        passwordIsEmpty.value = true
                    } else {
                        passwordIsEmpty.value = false
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
                EmailTextField(
                    isEmpty = emailIsEmpty.value,
                    value = email.value
                ) {
                    email.value = it
                    emailIsEmpty.value = email.value.isEmpty()
                }
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = {
                        if (!passwordIsEmpty.value && !emailIsEmpty.value) {
                            viewModel.updateEmail(password.value, email.value)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text(text = stringResource(R.string.confirm))
                }
            }
        }

    }
}

@Composable
private fun PasswordTextField(
    value: String,
    isEmpty: Boolean,
    onValueChange: (String) -> Unit
) {
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    val isVisible = remember {
        mutableStateOf(false)
    }
    val visualTransformation = remember {
        mutableStateOf(VisualTransformation.None)
    }
    LaunchedEffect(key1 = isVisible.value) {
        if (isVisible.value) {
            visualTransformation.value = VisualTransformation.None
        } else {
            visualTransformation.value = PasswordVisualTransformation()
        }
    }
    TextField(
        isError = isEmpty,
        modifier = Modifier.width(screenWith / 1.2f),
        visualTransformation = visualTransformation.value,
        trailingIcon =
        {
            Icon(painter = painterResource(id = if (isVisible.value) R.drawable.rounded_visibility_24 else R.drawable.rounded_visibility_off_24),
                contentDescription = "visibility",
                modifier = Modifier.clickable { isVisible.value = !isVisible.value })
        },
        supportingText = if (isEmpty) {
            { Text(text = stringResource(R.string.password_cannot_be_empty)) }
        } else null,
        singleLine = true,
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(text = stringResource(R.string.password)) },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        leadingIcon = { Icon(imageVector = Icons.Rounded.Key, contentDescription = "password") },
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun EmailTextField(
    value: String,
    isEmpty: Boolean,
    onValueChange: (String) -> Unit
) {
    val screenWith = LocalConfiguration.current.screenWidthDp.dp
    TextField(
        isError = isEmpty,
        supportingText = if (isEmpty) {
            { Text(text = stringResource(R.string.email_cannot_be_empty)) }
        } else null,
        modifier = Modifier.width(screenWith / 1.2f),
        singleLine = true,
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(text = stringResource(R.string.new_email_address)) },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        leadingIcon = { Icon(imageVector = Icons.Rounded.Email, contentDescription = "e mail") },
        shape = RoundedCornerShape(10.dp)
    )
}

@Preview
@Composable
private fun ResetEmailPreview() {
    //ResetEmail(navController = rememberNavController())
}