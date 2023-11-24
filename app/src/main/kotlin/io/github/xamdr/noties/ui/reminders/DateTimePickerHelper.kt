package io.github.xamdr.noties.ui.reminders

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class DateTimePickerHelper {

	val dates = listOf(
		today(),
		tomorrow(),
		nextWeek(),
		null
	)

	val times = listOf(
		fiveMinutes(),
		fifteenMinutes(),
		thirtyMinutes(),
		oneHour(),
		null
	)

	private fun today() = LocalDate.now()

	private fun tomorrow() = LocalDate.now().plusDays(1)

	private fun nextWeek() = LocalDate.now().plusDays(7)

	private fun fiveMinutes() = LocalTime.now().plusMinutes(5)

	private fun fifteenMinutes() = LocalTime.now().plusMinutes(15)

	private fun thirtyMinutes() = LocalTime.now().plusMinutes(30)

	private fun oneHour() = LocalTime.now().plusHours(1)
}

fun LocalDate?.asString(): String {
	return this?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: String.Empty
}

fun LocalTime?.asString(): String {
	return this?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) ?: String.Empty
}

fun Long.toLocalDate(): LocalDate {
	return Instant.ofEpochMilli(this)
		.atZone(ZoneId.systemDefault())
		.toLocalDate()
}

fun Long.toLocalTime(): LocalTime {
	return Instant.ofEpochMilli(this)
		.atZone(ZoneId.systemDefault())
		.toLocalTime()
}