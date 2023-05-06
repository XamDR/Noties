@file:Suppress("DEPRECATION")

package io.github.xamdr.noties.ui.helpers.task

import android.app.ProgressDialog
import android.content.Context
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.ui.helpers.showToast

sealed interface Result {
	object Failure: Result
	object Success: Result
}

class SaveMediaItemTask(private val context: Context, private val progressDialog: ProgressDialog) : CoroutineTask<Image, Unit, Result>() {

	override fun onPreExecute() {
		progressDialog.show()
	}

	override fun doInBackground(vararg params: Image): Result {
		TODO()
	}

	override fun onPostExecute(result: Result) {
		progressDialog.hide()
		when (result) {
			Result.Failure -> context.showToast(R.string.error_save_file)
			Result.Success -> context.showToast(R.string.app_name)
		}
	}
}