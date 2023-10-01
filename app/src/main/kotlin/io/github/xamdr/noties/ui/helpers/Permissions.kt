package io.github.xamdr.noties.ui.helpers

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

class PermissionState(private val permission: String, private val context: Context) {

	var hasPermission by mutableStateOf(value = getPermissionStatus())
	var launcher: ActivityResultLauncher<String>? = null

	fun launchPermissionRequest() {
		launcher?.launch(permission) ?: throw IllegalArgumentException("$launcher is null")
	}

	fun update() {
		hasPermission = getPermissionStatus()
	}

	private fun getPermissionStatus() =
		ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun rememberPermissionState(
	permission: String,
	onPermissionResult: (Boolean) -> Unit = {}
): PermissionState {
	val context = LocalContext.current
	val permissionState = remember(permission) {
		PermissionState(permission, context)
	}

	val permissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
		onResult = onPermissionResult
	)
	
	DisposableEffect(key1 = permissionState, key2 = permissionLauncher) {
		permissionState.launcher = permissionLauncher
		onDispose {
			permissionState.launcher = null
		}
	}
	return permissionState
}

@Composable
fun PermissionRationaleDialog(icon: ImageVector, message: Int) {
	Column(
		modifier = Modifier.wrapContentSize()
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(color = MaterialTheme.colorScheme.primary)
				.padding(40.dp)
		) {
			Image(
				imageVector = icon,
				contentDescription = null,
				modifier = Modifier.align(alignment = Alignment.Center)
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

@DevicePreviews
@Composable
private fun PermissionRationaleDialogPreview() = NotiesTheme {
	PermissionRationaleDialog(
		icon = Icons.Outlined.Mic,
		message = R.string.audio_recording_permission_rationale
	)
}

//@Composable
//fun PermissionLauncher(
//	permissionName: String,
//	onPermissionGranted: () -> Unit,
//	onPermissionDenied: () -> Unit
//) {
//	val context = LocalContext.current
//
//	fun onResult(granted: Boolean) = if (granted) onPermissionGranted() else onPermissionDenied()
//
//	val permissionLauncher = rememberLauncherForActivityResult(
//		contract = ActivityResultContracts.RequestPermission(),
//		onResult = ::onResult
//	)
//
//	fun executeOrLaunch() {
//		if (ContextCompat.checkSelfPermission(context, permissionName) != PackageManager.PERMISSION_GRANTED) {
//			permissionLauncher.launch(permissionName)
//		}
//		else {
//			onPermissionGranted()
//		}
//	}
//}