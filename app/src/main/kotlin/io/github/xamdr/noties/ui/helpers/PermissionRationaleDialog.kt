package io.github.xamdr.noties.ui.helpers

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
	val configuration = LocalConfiguration.current
	val iconPadding = if (configuration.screenHeightDp >= 600) 20.dp else 10.dp
	val textPadding = if (configuration.screenHeightDp >= 600) 40.dp else 30.dp

	fun onPermissionResult(granted: Boolean) {
		if (granted) {
			onPermissionGranted()
		}
		else {
			onPermissionDenied()
		}
		onDismiss()
	}

	val permissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
		onResult = ::onPermissionResult
	)

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false)
	) {
		Surface(modifier = Modifier.fillMaxSize()) {
			Column(
				modifier = Modifier.fillMaxSize(),
				verticalArrangement = Arrangement.SpaceBetween,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(all = 16.dp)
				) {
					Icon(
						imageVector = Icons.Outlined.Close,
						contentDescription = stringResource(R.string.close_dialog),
						modifier = Modifier
							.size(size = 32.dp)
							.align(alignment = Alignment.TopStart)
							.clickable(onClick = onDismiss)
					)
				}
				Column(
					modifier = Modifier.fillMaxSize(),
					verticalArrangement = Arrangement.Center,
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 40.dp, vertical = iconPadding)
					) {
						Icon(
							imageVector = icon,
							contentDescription = null,
							modifier = Modifier
								.size(size = 48.dp)
								.align(alignment = Alignment.Center)
						)
					}
					Text(
						text = stringResource(id = message),
						modifier = Modifier
							.fillMaxWidth()
							.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = textPadding),
						fontSize = 16.sp,
						textAlign = TextAlign.Justify
					)
					Button(
						onClick = { permissionLauncher.launch(permission) },
						modifier = Modifier.padding(all = 4.dp)
					) {
						Text(text = stringResource(id = R.string.continue_button))
					}
					TextButton(
						onClick = onDismiss,
						modifier = Modifier.padding(all = 4.dp)
					) {
						Text(text = stringResource(id = R.string.not_now_button))
					}
				}
			}
		}
	}
}

@DevicePreviews
@Composable
private fun PermissionRationaleDialogPreview() {
	NotiesTheme {
		PermissionRationaleDialog(
			icon = Icons.Outlined.Mic,
			message = R.string.write_external_storage_permission_rationale,
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
	action: () -> Unit,
	requestPermission: () -> Unit,
	condition: (() -> Boolean)? = null
): () -> Unit {
	val context = LocalContext.current
	return {
		if (condition?.invoke() == true) {
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