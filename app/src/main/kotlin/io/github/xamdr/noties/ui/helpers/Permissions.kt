package io.github.xamdr.noties.ui.helpers

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun PermissionLauncher(
	permissionName: String,
	messageRes: Int,
	icon: ImageVector,
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