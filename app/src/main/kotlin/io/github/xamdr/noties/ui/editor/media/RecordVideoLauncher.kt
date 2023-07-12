package io.github.xamdr.noties.ui.editor.media

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.PermissionLauncher
import io.github.xamdr.noties.ui.helpers.getParcelableCompat
import io.github.xamdr.noties.ui.helpers.media.MediaStorageManager
import io.github.xamdr.noties.ui.helpers.showToast
import javax.inject.Inject

class RecordVideoLauncher @Inject constructor(
	@ApplicationContext private val context: Context,
	private val registry: ActivityResultRegistry,
	registryOwner: SavedStateRegistryOwner,
	private val onSuccess: (uri: Uri) -> Unit,
	private val onError: () -> Unit) : DefaultLifecycleObserver, SavedStateRegistry.SavedStateProvider {

	private lateinit var recordVideoLauncher: ActivityResultLauncher<Uri>
	private lateinit var externalStoragePermissionLauncher: PermissionLauncher
	private lateinit var videoUri: Uri

	init {
		registryOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_CREATE) {
				val registry = registryOwner.savedStateRegistry
				registry.registerSavedStateProvider(PROVIDER, this)
				val restoredState = registry.consumeRestoredStateForKey(PROVIDER)
				if (restoredState != null && restoredState.containsKey(VIDEO_URI_BUNDLE)) {
					videoUri = restoredState.getParcelableCompat(VIDEO_URI_BUNDLE, Uri::class.java)
				}
			}
		})
	}

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

	override fun saveState(): Bundle {
		return if (::videoUri.isInitialized) {
			bundleOf(VIDEO_URI_BUNDLE to videoUri)
		}
		else {
			bundleOf()
		}
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
		private const val VIDEO_URI_BUNDLE = "VIDEO_URI_BUNDLE"
		private const val PROVIDER = "RECORD_VIDEO_LAUNCHER"
	}
}