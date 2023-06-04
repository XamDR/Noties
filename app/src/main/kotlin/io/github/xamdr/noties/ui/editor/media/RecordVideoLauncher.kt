package io.github.xamdr.noties.ui.editor.media

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.PermissionLauncher
import io.github.xamdr.noties.ui.helpers.media.MediaStorageManager
import io.github.xamdr.noties.ui.helpers.showToast
import javax.inject.Inject

class RecordVideoLauncher @Inject constructor(
	@ApplicationContext private val context: Context,
	private val registry: ActivityResultRegistry,
	private val onSuccess: (uri: Uri) -> Unit,
	private val onError: () -> Unit) : DefaultLifecycleObserver {

	private lateinit var recordVideoLauncher: ActivityResultLauncher<Uri>
	private lateinit var externalStoragePermissionLauncher: PermissionLauncher
	private lateinit var videoUri: Uri

	override fun onCreate(owner: LifecycleOwner) {
		recordVideoLauncher = registry.register(RECORD_VIDEO_LAUNCHER_KEY, ActivityResultContracts.CaptureVideo()) { success ->
			if (success) {
				onSuccess(videoUri)
			}
			else {
				onError()
			}
		}
		externalStoragePermissionLauncher = PermissionLauncher(context, registry,
			onPermissionGranted = { recordVideo() },
			onPermissionDenied = { context.showToast(R.string.permission_denied) }
		)
		owner.lifecycle.addObserver(externalStoragePermissionLauncher)
	}

	override fun onDestroy(owner: LifecycleOwner) {
		owner.lifecycle.removeObserver(externalStoragePermissionLauncher)
	}

	fun launch() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			recordVideo()
		}
		else {
			externalStoragePermissionLauncher.executeOrLaunch(
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				R.string.write_external_storage_permission_rationale,
				R.drawable.ic_external_storage
			)
		}
	}

	private fun recordVideo() {
		videoUri = MediaStorageManager.saveVideo(context) ?: return
		recordVideoLauncher.launch(videoUri)
	}

	private companion object {
		private const val RECORD_VIDEO_LAUNCHER_KEY = "RECORD_VIDEO_LAUNCHER_KEY"
	}
}