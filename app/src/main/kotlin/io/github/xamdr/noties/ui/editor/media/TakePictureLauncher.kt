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

class TakePictureLauncher @Inject constructor(
	@ApplicationContext private val context: Context,
	private val registry: ActivityResultRegistry,
	registryOwner: SavedStateRegistryOwner,
	private val onSuccess: (uri: Uri) -> Unit,
	private val onError: () -> Unit) : DefaultLifecycleObserver, SavedStateRegistry.SavedStateProvider {

	private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
	private lateinit var externalStoragePermissionLauncher: PermissionLauncher
	private lateinit var cameraUri: Uri

	init {
		registryOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
			if (event == Lifecycle.Event.ON_CREATE) {
				val registry = registryOwner.savedStateRegistry
				registry.registerSavedStateProvider(PROVIDER, this)
				val restoredState = registry.consumeRestoredStateForKey(PROVIDER)
				if (restoredState != null && restoredState.containsKey(CAMERA_URI_BUNDLE)) {
					cameraUri = restoredState.getParcelableCompat(CAMERA_URI_BUNDLE, Uri::class.java)
				}
			}
		})
	}

	override fun onCreate(owner: LifecycleOwner) {
		takePictureLauncher = registry.register(TAKE_PICTURE_LAUNCHER_KEY, ActivityResultContracts.TakePicture()) { success ->
			if (success) {
				onSuccess(cameraUri)
			}
			else {
				onError()
			}
		}
		externalStoragePermissionLauncher = PermissionLauncher(context, registry,
			onPermissionGranted = { takePicture() },
			onPermissionDenied = { context.showToast(R.string.permission_denied) }
		)
		owner.lifecycle.addObserver(externalStoragePermissionLauncher)
	}

	override fun onDestroy(owner: LifecycleOwner) {
		owner.lifecycle.removeObserver(externalStoragePermissionLauncher)
	}

	override fun saveState(): Bundle {
		return if (::cameraUri.isInitialized) {
			bundleOf(CAMERA_URI_BUNDLE to cameraUri)
		}
		else {
			bundleOf()
		}
	}

	fun launch() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			takePicture()
		}
		else {
			externalStoragePermissionLauncher.executeOrLaunch(
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				R.string.write_external_storage_permission_rationale,
				R.drawable.ic_external_storage
			)
		}
	}

	private fun takePicture() {
		cameraUri = MediaStorageManager.savePicture(context) ?: return
		takePictureLauncher.launch(cameraUri)
	}

	private companion object {
		private const val TAKE_PICTURE_LAUNCHER_KEY = "TAKE_PICTURE_LAUNCHER_KEY"
		private const val CAMERA_URI_BUNDLE = "CAMERA_URI_BUNDLE"
		private const val PROVIDER = "TAKE_PICTURE_LAUNCHER"
	}
}