@file:Suppress("DEPRECATION")

package io.github.xamdr.noties.ui.helpers

import android.app.ProgressDialog
import android.content.Context
import android.graphics.PorterDuff
import android.widget.ProgressBar
import com.google.android.material.color.MaterialColors
import io.github.xamdr.noties.R
import timber.log.Timber

object ProgressDialogHelper {

	private lateinit var progressDialog: ProgressDialog

	fun show(context: Context, message: CharSequence, cancelable: Boolean = false) {
		progressDialog = ProgressDialog(context)
		progressDialog.apply {
			setCancelable(cancelable)
			setMessage(message)
			show()
			tintProgressBar(this)
		}
	}

	fun dismiss() {
		if (::progressDialog.isInitialized && progressDialog.isShowing) {
			progressDialog.dismiss()
		}
		else {
			Timber.d("ProgressDialog has not been initialized")
		}
	}

	private fun tintProgressBar(progressDialog: ProgressDialog) {
		val progressBar = progressDialog.findViewById<ProgressBar>(android.R.id.progress)
		progressBar.indeterminateDrawable.setColorFilter(
			MaterialColors.getColor(progressBar, R.attr.colorPrimary),
			PorterDuff.Mode.SRC_IN
		)
	}
}