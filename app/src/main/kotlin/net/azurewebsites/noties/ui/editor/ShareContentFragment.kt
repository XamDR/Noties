package net.azurewebsites.noties.ui.editor

import android.content.Intent
import android.net.Uri
import net.azurewebsites.noties.R
import net.azurewebsites.noties.ui.helpers.showToast

class ShareContentFragment : BaseHeadlessChildFragment() {

	fun shareContent() {
		if (viewModel.note.isNonEmpty()) {
			if (viewModel.note.images.isEmpty()) {
				shareText(viewModel.entity.text)
			}
			else {
				shareImagesAndText(
					viewModel.note.images.map { it.uri },
					viewModel.entity.text
				)
			}
		}
		else {
			context?.showToast(R.string.empty_note_share)
		}
	}

	private fun shareText(text: String) {
		val shareIntent = Intent().apply {
			action = Intent.ACTION_SEND
			putExtra(Intent.EXTRA_TEXT, text)
			type = EditorFragment.MIME_TYPE_TEXT
		}
		startActivity(Intent.createChooser(shareIntent, getString(R.string.chooser_dialog_title)))
	}

	private fun shareImagesAndText(images: List<Uri?>, text: String) {
		val shareIntent = Intent().apply {
			action = Intent.ACTION_SEND_MULTIPLE
			putExtra(Intent.EXTRA_TEXT, text)
			putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(images))
			type = EditorFragment.MIME_TYPE_IMAGE
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		startActivity(Intent.createChooser(shareIntent, getString(R.string.chooser_dialog_title)))
	}
}