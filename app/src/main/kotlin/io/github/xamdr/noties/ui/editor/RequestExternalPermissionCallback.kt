package io.github.xamdr.noties.ui.editor

import androidx.activity.result.ActivityResultCallback

class RequestExternalPermissionCallback(private val action: () -> Unit) : ActivityResultCallback<Boolean> {

	override fun onActivityResult(result: Boolean) {
		if (result) {
			action()
		}
		else {
			onPermissionDeniedCallback()
		}
	}

	fun setOnPermissionDeniedListener(callback: () -> Unit) {
		onPermissionDeniedCallback = callback
	}

	private var onPermissionDeniedCallback: () -> Unit = {}
}