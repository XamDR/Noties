package io.github.xamdr.noties.ui.helpers

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun PermissionRationaleDialog() {
	Column(
		modifier = Modifier.wrapContentSize()
	) {
		// Header Container
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.background(color = MaterialTheme.colorScheme.primary)
				.padding(40.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			// Here you can add the permission's icon
		}

		// Message Permission Rationale
		Text(
			text = "Permission Rationale Message",
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					start = 20.dp,
					end = 20.dp,
					top = 40.dp,
					bottom = 40.dp
				),
			fontSize = 15.sp
		)
	}
}

@DevicePreviews
@Composable
private fun PermissionRationaleDialogPreview() = NotiesTheme { PermissionRationaleDialog() }

@Composable
fun PermissionLauncher(
	permissionName: String,
	onPermissionGranted: () -> Unit,
	onPermissionDenied: () -> Unit
) {
	val context = LocalContext.current

	fun onResult(granted: Boolean) = if (granted) onPermissionGranted() else onPermissionDenied()

	val permissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
		onResult = ::onResult
	)

	fun executeOrLaunch() {
		if (ContextCompat.checkSelfPermission(context, permissionName) != PackageManager.PERMISSION_GRANTED) {
			permissionLauncher.launch(permissionName)
		}
		else {
			onPermissionGranted()
		}
	}
}