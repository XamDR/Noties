package net.azurewebsites.noties.ui.helpers

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object DateTimeConverter {

	@JvmStatic
	fun formatCurrentDateTime(currentDateTime: ZonedDateTime): String {
		val formatter = DateTimeFormatter.ofPattern(PATTERN).withZone(ZoneId.systemDefault())
		return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).format(
			ZonedDateTime.parse(formatter.format(currentDateTime), formatter)
		)
	}

	private const val PATTERN = "dd/MM/yyyy HH:mm:ss"
}