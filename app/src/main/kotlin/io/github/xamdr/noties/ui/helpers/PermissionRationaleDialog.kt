package io.github.xamdr.noties.ui.helpers

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun PermissionRationaleDialog(
	icon: ImageVector,
	message: Int,
	permission: String,
	onPermissionGranted: () -> Unit,
	onPermissionDenied: () -> Unit,
	onDismiss: () -> Unit,
) {
	val permissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
		onResult = { granted ->
			if (granted) onPermissionGranted() else onPermissionDenied()
		}
	)

	fun onContinue() {
		onDismiss()
		permissionLauncher.launch(permission)
	}

	AlertDialog(
		onDismissRequest = onDismiss,
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(text = stringResource(id = R.string.not_now_button))
			}
		},
		confirmButton = {
			TextButton(onClick = ::onContinue) {
				Text(text = stringResource(id = R.string.continue_button))
			}
		},
		text = {
			Column(modifier = Modifier.wrapContentSize()) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.background(color = MaterialTheme.colorScheme.primary)
						.padding(40.dp)
				) {
					Icon(
						imageVector = icon,
						contentDescription = null,
						modifier = Modifier
							.size(size = 48.dp)
							.align(alignment = Alignment.Center),
						tint = MaterialTheme.colorScheme.background
					)
				}
				Text(
					text = stringResource(id = message),
					modifier = Modifier
						.fillMaxWidth()
						.padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 40.dp),
					fontSize = 15.sp,
					textAlign = TextAlign.Justify
				)
			}
		}
	)
}

@DevicePreviews
@Composable
private fun PermissionRationaleDialogPreview() {
	NotiesTheme {
		PermissionRationaleDialog(
			icon = Icons.Outlined.Mic,
			message = R.string.audio_recording_permission_rationale,
			permission = "",
			onPermissionGranted = {},
			onPermissionDenied = {},
			onDismiss = {}
		)
	}
}

@Composable
fun doActionOrRequestPermission(
	permission: String,
	condition: () -> Boolean,
	action: () -> Unit,
	requestPermission: () -> Unit
): () -> Unit {
	val context = LocalContext.current
	return {
		if (condition()) {
			action()
		}
		else {
			if (context.hasPermission(permission)) {
				action()
			}
			else {
				requestPermission()
			}
		}
	}
}

fun Context.hasPermission(permission: String): Boolean {
	return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}