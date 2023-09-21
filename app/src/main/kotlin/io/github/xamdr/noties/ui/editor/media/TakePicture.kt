package io.github.xamdr.noties.ui.editor.media

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.github.xamdr.noties.ui.helpers.media.MediaStorageManager

@Composable
fun TakePicture(
	onSuccess: (uri: Uri) -> Unit,
	onError: () -> Unit
) {
	val context = LocalContext.current
	var cameraUri by rememberSaveable { mutableStateOf(value = Uri.EMPTY) }

	val externalStoragePermissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
		onResult = {}
	)

	fun onResult(success: Boolean) = if (success) onSuccess(cameraUri) else onError()

	val takePictureLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.TakePicture(),
		onResult = ::onResult
	)

	fun takePicture() {
		cameraUri = MediaStorageManager.savePicture(context) ?: return
		takePictureLauncher.launch(cameraUri)
	}
}