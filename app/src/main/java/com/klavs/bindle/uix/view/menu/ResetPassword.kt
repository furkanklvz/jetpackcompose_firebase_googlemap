package com.klavs.bindle.uix.view.menu

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseUser
import com.klavs.bindle.R
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.uix.view.auth.PasswordAgainTextField
import com.klavs.bindle.uix.view.auth.PasswordTextField
import com.klavs.bindle.uix.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPassword(
    navController: NavHostController, currentUser: FirebaseUser,
    viewModel: ProfileViewModel
) {
    val context = LocalContext.current
    val currentPassword = remember {
        mutableStateOf("")
    }
    val newPassword = remember {
        mutableStateOf("")
    }
    val newPasswordAgain = remember {
        mutableStateOf("")
    }
    val passwordIsValid = remember {
        mutableStateOf(false)
    }
    val forgotPasswordDialogIsEnable = remember {
        mutableStateOf(false)
    }
    val currentPasswordIsWrong = remember {
        mutableStateOf(false)
    }
    val emailSent = remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
        CenterAlignedTopAppBar(navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "turn back"
                )
            }
        }, title = { Text(text = stringResource(R.string.reset_password)) })
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding() + 20.dp)
        ) {
            LaunchedEffect(key1 = viewModel.resetPasswordState.value) {
                when (val resource = viewModel.resetPasswordState.value) {
                    is Resource.Error -> {
                        scope.launch { snackbarHostState.showSnackbar(resource.messageResource?.let { context.getString(it) }?:"") }
                    }

                    is Resource.Idle -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        if (resource.data!!) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.password_changed_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        } else {
                            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.wrong_password_please_check_it)) }
                        }
                    }
                }
            }

            if (forgotPasswordDialogIsEnable.value) {
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
                            Text(text = stringResource(R.string.sending_changing_password_link_dialog_text))
                        }

                    },
                    onDismissRequest = { forgotPasswordDialogIsEnable.value = false },
                    confirmButton = {

                        Button(enabled = !emailSent.value, onClick = {
                            if (currentUser.email != null) {
                                viewModel.sendResetPasswordEmail(
                                    email = currentUser.email!!
                                )
                                emailSent.value = true
                            }else{
                                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.email_not_found)) }
                            }
                        }) {
                            Text(text = if (emailSent.value) stringResource(R.string.link_sent_check_email)
                            else stringResource(R.string.send_link_to_reset_password))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { forgotPasswordDialogIsEnable.value = false }) {
                            Text(text = stringResource(R.string.cancel))
                        }
                    })
            }
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                PasswordTextField(
                    iconIsEnable = false,
                    isResettingPassword = true,
                    value = currentPassword.value,
                    onValueChange = { currentPassword.value = it },
                    label = stringResource(R.string.current_password)
                )
                Spacer(modifier = Modifier.height(40.dp))
                PasswordTextField(
                    iconIsEnable = false,
                    error = !passwordIsValid.value,
                    value = newPassword.value,
                    onValueChange = {
                        newPassword.value = it
                        passwordIsValid.value = newPassword.value.length >= 6
                    },
                    label = stringResource(R.string.new_password)
                )
                Spacer(modifier = Modifier.height(20.dp))

                PasswordAgainTextField(
                    iconIsEnable = false,
                    isPasswordValid = passwordIsValid.value,
                    value = newPasswordAgain.value,
                    passwordsDifferent = newPassword.value != newPasswordAgain.value,
                    onValueChange = { newPasswordAgain.value = it },
                    label = stringResource(R.string.new_password_again)
                )
                Row(Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {forgotPasswordDialogIsEnable.value = true}) {
                        Text(text = stringResource(R.string.forgot_password))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(onClick = {
                    if (passwordIsValid.value && newPassword.value == newPasswordAgain.value && viewModel.resetPasswordState.value !is Resource.Loading) {
                        viewModel.resetPassword(
                            currentPassword = currentPassword.value,
                            newPassword.value
                        )
                    }
                })
                {
                    Text(text = stringResource(R.string.change_password))
                }
            }
        }

    }
}

@Preview
@Composable
private fun ResetPasswordPreview() {
    //ResetPassword(navController = rememberNavController())
}