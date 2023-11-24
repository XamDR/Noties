package io.github.xamdr.noties.ui.reminders

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

sealed class ReminderDate(val value: LocalDate?) {
	class Today(value: LocalDate): ReminderDate(value)
	class Tomorrow(value: LocalDate): ReminderDate(value)
	class NextWeek(value: LocalDate): ReminderDate(value)
	class CustomDate(value: LocalDate?): ReminderDate(value)

	override fun toString(): String =
		value?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: String.Empty
}

sealed class ReminderTime(val value: LocalTime?) {
	class FiveMinutes(value: LocalTime): ReminderTime(value)
	class FifteenMinutes(value: LocalTime): ReminderTime(value)
	class ThirtyMinutes(value: LocalTime): ReminderTime(value)
	class OneHour(value: LocalTime): ReminderTime(value)
	class CustomTime(value: LocalTime?): ReminderTime(value)

	override fun toString() =
		value?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) ?: String.Empty
}

class DateTimePickerHelper {

	val dates = listOf(
		ReminderDate.Today(value = today()),
		ReminderDate.Tomorrow(value = tomorrow()),
		ReminderDate.NextWeek(value = nextWeek()),
		ReminderDate.CustomDate(value = null)
	)

	val times = listOf(
		ReminderTime.FiveMinutes(value = fiveMinutes()),
		ReminderTime.FifteenMinutes(value = fifteenMinutes()),
		ReminderTime.ThirtyMinutes(value = thirtyMinutes()),
		ReminderTime.OneHour(value = oneHour()),
		ReminderTime.CustomTime(value = null)
	)

	private fun today() = LocalDate.now()

	private fun tomorrow() = LocalDate.now().plusDays(1)

	private fun nextWeek() = LocalDate.now().plusDays(7)

	private fun fiveMinutes() = LocalTime.now().plusMinutes(5)

	private fun fifteenMinutes() = LocalTime.now().plusMinutes(15)

	private fun thirtyMinutes() = LocalTime.now().plusMinutes(30)

	private fun oneHour() = LocalTime.now().plusHours(1)
}