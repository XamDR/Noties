package net.azurewebsites.noties.ui.editor

import android.net.Uri
import androidx.activity.result.ActivityResultCallback

class OpenFileCallback(private val listener: OpenFileListener) : ActivityResultCallback<Uri?> {

	override fun onActivityResult(result: Uri?) {
		listener.readFileContent(result)
	}
}