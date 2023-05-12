package io.github.xamdr.noties.ui.helpers

import android.content.Context
import androidx.core.app.ShareCompat
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.domain.model.Note

object ShareHelper {

	fun shareContent(context: Context, note: Note) {
		if (note.images.isEmpty()) {
			shareText(context, note.text)
		}
		else {
			shareTextAndMedia(context, note.images, note.text)
		}
	}

	private fun shareText(context: Context, text: String) {
		ShareCompat.IntentBuilder(context)
			.setType(Constants.MIME_TYPE_TEXT)
			.setText(text)
			.setChooserTitle(R.string.chooser_dialog_title)
			.startChooser()
	}

	private fun shareTextAndMedia(context: Context, items: List<Image>, text: String) {
		val shareIntent = ShareCompat.IntentBuilder(context)
			.setType(Constants.MIME_TYPE_IMAGE)
			.setText(text)
			.setChooserTitle(R.string.chooser_dialog_title)
		for (item in items) {
			item.uri?.let { shareIntent.addStream(it) }
		}
		shareIntent.startChooser()
	}
}