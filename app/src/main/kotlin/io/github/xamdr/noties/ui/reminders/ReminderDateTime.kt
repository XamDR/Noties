package io.github.xamdr.noties.ui.reminders

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

sealed class ReminderDate(val name: String, val value: LocalDate?) {
	class Today(name: String, value: LocalDate): ReminderDate(name, value)
	class Tomorrow(name: String, value: LocalDate): ReminderDate(name, value)
	class NextWeek(name: String, value: LocalDate): ReminderDate(name, value)
	class CustomDate(name: String, value: LocalDate?): ReminderDate(name, value)

	override fun toString(): String =
		value?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: String.Empty
}

sealed class ReminderTime(val name: String, val value: LocalTime?) {
	class TenMinutes(name: String, value: LocalTime): ReminderTime(name, value)
	class FifteenMinutes(name: String, value: LocalTime): ReminderTime(name, value)
	class ThirtyMinutes(name: String, value: LocalTime): ReminderTime(name, value)
	class OneHour(name: String, value: LocalTime): ReminderTime(name, value)
	class CustomTime(name: String, value: LocalTime?): ReminderTime(name, value)

	override fun toString() =
		value?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) ?: String.Empty
}