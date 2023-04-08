package io.github.xamdr.noties.ui.editor

import android.net.Uri
import androidx.core.content.FileProvider
import io.github.xamdr.noties.core.ImageEntity
import io.github.xamdr.noties.ui.helpers.getUriExtension
import io.github.xamdr.noties.ui.helpers.getUriMimeType
import io.github.xamdr.noties.ui.image.ImageStorageManager
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddImagesFragment : BaseHeadlessChildFragment() {

	suspend fun addImages(uris: List<Uri>) {
		for (uri in uris) {
			val newUri = copyUri(uri)
			val image = ImageEntity(
				uri = newUri,
				mimeType = requireContext().getUriMimeType(newUri),
				noteId = viewModel.entity.id
			)
			viewModel.note.images += image
		}
	}

	private suspend fun copyUri(uri: Uri): Uri {
		val extension = requireContext().getUriExtension(uri) ?: DEFAULT_IMAGE_EXTENSION
		val fileName = buildString {
			append("IMG_")
			append(DateTimeFormatter.ofPattern(PATTERN).format(LocalDateTime.now()))
			append("_${(0..999).random()}.$extension")
		}
		val fullPath = ImageStorageManager.saveImage(requireContext(), uri, fileName)
		val file = File(fullPath)
		return FileProvider.getUriForFile(requireContext(), AUTHORITY, file)
	}

	private companion object {
		private const val AUTHORITY = "io.github.xamdr.noties"
		private const val DEFAULT_IMAGE_EXTENSION = "jpeg"
		private const val PATTERN = "yyyyMMdd_HHmmss"
	}
}