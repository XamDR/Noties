package io.github.xamdr.noties.data.database

import android.net.Uri
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ZonedDateTimeToStringConverter {

	@TypeConverter
	fun fromZonedDateTime(dateTime: LocalDateTime?): String? {
		val formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault())
		return if (dateTime != null) formatter.format(dateTime) else null
	}

	@TypeConverter
	fun toZonedDateTime(value: String?): LocalDateTime? {
		val formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault())
		return if (value != null) LocalDateTime.parse(value, formatter) else null
	}

	companion object {
		private const val pattern = "dd/MM/yyyy HH:mm:ss"
	}
}

class StringArrayToStringConverter {
	@TypeConverter
	fun fromStringArray(strArray: List<String>): String = strArray.joinToString(SEPARATOR)

	@TypeConverter
	fun toStringArray(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split(DELIMITER)

	companion object {
		private const val SEPARATOR = "|"
		private const val DELIMITER = SEPARATOR
	}
}

class UriToStringConverter {
	@TypeConverter
	fun fromUri(uri: Uri?): String? = uri?.toString()

	@TypeConverter
	fun toUri(value: String?): Uri? = if (value != null) Uri.parse(value) else null
}
