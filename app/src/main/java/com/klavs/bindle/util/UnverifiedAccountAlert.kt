package com.klavs.bindle.util

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.klavs.bindle.R

@Composable
fun UnverifiedAccountAlertDialog(onDismiss: () -> Unit, onVerifyNow : () -> Unit) {
    AlertDialog(
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.later))
            }
        },
        confirmButton = {
            FilledTonalButton(
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = ButtonDefaults.filledTonalButtonColors().containerColor,
                    contentColor = ButtonDefaults.filledTonalButtonColors().contentColor
                ),
                onClick = onVerifyNow
            ) {
                Text(stringResource(R.string.verify_now))
            }
        },
        onDismissRequest = onDismiss,
        text = {
            Text(stringResource(R.string.unverified_account_alert_dialog_text))
        },
        title = {
            Text(stringResource(R.string.unverified_account_alert_dialog_title))
        },
        icon = {
            Icon(
                painter = painterResource(R.drawable.person_alert),
                contentDescription = "unverified account"
            )
        }
    )
}


@Preview
@Composable
private fun UnverifiedAccountAlertDialogPreview() {
    UnverifiedAccountAlertDialog(
        onDismiss = {},
        onVerifyNow = {}
    )
}