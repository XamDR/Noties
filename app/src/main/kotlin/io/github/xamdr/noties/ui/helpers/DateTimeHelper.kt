package io.github.xamdr.noties.ui.helpers

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.Locale

object DateTimeHelper {

	private val formatter = DateTimeFormatter
		.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
		.withZone(ZoneId.systemDefault())

	fun formatDateTime(value: Long): String {
		val instant = Instant.ofEpochMilli(value)
		return formatter.format(instant)
	}

	fun isValidDate(input: String): Boolean {
		return try {
			LocalDateTime.parse(input, formatter)
			true
		}
		catch (e: DateTimeParseException) {
			false
		}
	}

	fun isPast(input: String): Boolean {
		val localDateTime = LocalDateTime.parse(input, formatter)
		return isPast(localDateTime.toLocalDate(), localDateTime.toLocalTime())
	}

	fun isPast(localDate: LocalDate, localTime: LocalTime): Boolean {
		return localDate.isBefore(LocalDate.now()) ||
				(localDate.isEqual(LocalDate.now()) && localTime.isBefore(LocalTime.now()))
	}

	private fun getDateTimeWithoutYear(`when`: Long, zoneId: ZoneId, locale: Locale): String {
		val pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.MEDIUM, FormatStyle.SHORT, IsoChronology.INSTANCE, locale)
		val keep = listOf('d', 'M', 'H', 'm')
		var separator = false
		var copy = true
		var newPattern = StringBuilder()

		for (c in pattern.toCharArray()) {
			if (c == '\'') {
				separator = !separator
			}
			if (!separator) {
				if (c.isLetter()) {
					copy = keep.contains(c)
				}
			}
			if (copy) {
				newPattern.append(c)
			}
		}
		var lastChar = newPattern[newPattern.length - 1]
		while (!keep.contains(lastChar)) {
			newPattern = StringBuilder(newPattern.substring(0, newPattern.length - 1))
			if (lastChar == '\'') {
				newPattern = StringBuilder(newPattern.substring(0, newPattern.toString().lastIndexOf('\'')))
			}
			lastChar = newPattern[newPattern.length - 1]
		}
		val dateTime = Instant.ofEpochMilli(`when`).atZone(zoneId).toLocalDateTime()
		return DateTimeFormatter.ofPattern(newPattern.toString(), locale).format(dateTime)
	}

	private fun isTodayOrYesterday(dateTime: LocalDateTime): Boolean {
		val today = LocalDate.now()
		val yesterday = today.minusDays(1)

		val date = dateTime.toLocalDate()
		return date == today || date == yesterday
	}

	private fun isCurrentYear(dateTime: LocalDateTime): Boolean {
		return dateTime.year == LocalDate.now().year
	}
}