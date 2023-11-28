package io.github.xamdr.noties.data.database

import android.net.Uri
import androidx.room.TypeConverter
import io.github.xamdr.noties.data.entity.media.MediaType

class StringArrayToStringConverter {
	@TypeConverter
	fun fromStringArray(strArray: List<String>): String = strArray.joinToString(separator = SEPARATOR, postfix = SEPARATOR)

	@TypeConverter
	fun toStringArray(value: String): List<String> =
		if (value.isEmpty()) emptyList() else value.trimEnd(DELIMITER).split(DELIMITER)

	private companion object {
		private const val SEPARATOR = "|"
		private const val DELIMITER = '|'
	}
}

class UriToStringConverter {
	@TypeConverter
	fun fromUri(uri: Uri?): String? = uri?.toString()

	@TypeConverter
	fun toUri(value: String?): Uri? = if (value != null) Uri.parse(value) else null
}

class MediaTypeToStringConverter {
	@TypeConverter
	fun fromMediaType(mediaType: MediaType): String = when (mediaType) {
		MediaType.Audio -> "audio"
		MediaType.Image -> "image"
		MediaType.Video -> "video"
	}

	@TypeConverter
	fun toMediaType(value: String): MediaType = when (value) {
		"image" -> MediaType.Image
		"video" -> MediaType.Video
		else -> MediaType.Audio
	}
}
