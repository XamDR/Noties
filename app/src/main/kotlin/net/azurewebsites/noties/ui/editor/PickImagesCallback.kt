package net.azurewebsites.noties.ui.editor

import android.net.Uri
import androidx.activity.result.ActivityResultCallback

class PickImagesCallback(private val listener: AttachImagesListener) : ActivityResultCallback<List<Uri>> {

	override fun onActivityResult(result: List<Uri>?) {
		if (!result.isNullOrEmpty()) {
			listener.addImages(result)
		}
	}
}