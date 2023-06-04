package io.github.xamdr.noties.ui.helpers

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.xamdr.noties.R
import javax.inject.Inject

class PermissionLauncher @Inject constructor(
	@ApplicationContext private val context: Context,
	private val registry: ActivityResultRegistry,
	private val onPermissionGranted: () -> Unit,
	private val onPermissionDenied: () -> Unit) : DefaultLifecycleObserver {

	private lateinit var permissionLauncher: ActivityResultLauncher<String>

	override fun onCreate(owner: LifecycleOwner) {
		permissionLauncher = registry.register(PERMISSION_LAUNCHER_KEY, ActivityResultContracts.RequestPermission()) { granted ->
			if (granted) {
				onPermissionGranted()
			}
			else {
				onPermissionDenied()
			}
		}
	}

	fun executeOrLaunch(permissionName: String, @StringRes messageRes: Int, @DrawableRes drawableRes: Int) {
		if (ContextCompat.checkSelfPermission(context, permissionName) != PackageManager.PERMISSION_GRANTED) {
			PermissionRationaleDialog.createFor(context, messageRes, drawableRes)
				.setNegativeButton(R.string.not_now_button, null)
				.setPositiveButton(R.string.continue_button) { _, _ ->
					permissionLauncher.launch(permissionName)
				}.show()
		}
		else {
			onPermissionGranted()
		}
	}

	private companion object {
		private const val PERMISSION_LAUNCHER_KEY = "PERMISSION_LAUNCHER_KEY"
	}
}