package io.github.xamdr.noties.ui.reminders

import io.github.xamdr.noties.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

sealed class ReminderDate(val name: Int, val value: LocalDate?) {
	class Today(name: Int, value: LocalDate): ReminderDate(name, value)
	class Tomorrow(name: Int, value: LocalDate): ReminderDate(name, value)
	class NextWeek(name: Int, value: LocalDate): ReminderDate(name, value)
	class CustomDate(name: Int, value: LocalDate?): ReminderDate(name, value)

	override fun toString(): String =
		value?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: String.Empty
}

sealed class ReminderTime(val name: Int, val value: LocalTime?) {
	class FiveMinutes(name: Int, value: LocalTime): ReminderTime(name, value)
	class FifteenMinutes(name: Int, value: LocalTime): ReminderTime(name, value)
	class ThirtyMinutes(name: Int, value: LocalTime): ReminderTime(name, value)
	class OneHour(name: Int, value: LocalTime): ReminderTime(name, value)
	class CustomTime(name: Int, value: LocalTime?): ReminderTime(name, value)

	override fun toString() =
		value?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) ?: String.Empty
}

val DATES = listOf(
	ReminderDate.Today(name = R.string.today, value = LocalDate.now()),
	ReminderDate.Tomorrow(name = R.string.tomorrow, value = LocalDate.now().plusDays(1)),
	ReminderDate.NextWeek(name = R.string.next_week, value = LocalDate.now().plusDays(7)),
	ReminderDate.CustomDate(name = R.string.select_date, value = null)
)

val TIMES = listOf(
	ReminderTime.FiveMinutes(name = R.string.five_minutes, value = LocalTime.now().plusMinutes(5)),
	ReminderTime.FifteenMinutes(name = R.string.fifteen_minutes, value = LocalTime.now().plusMinutes(15)),
	ReminderTime.ThirtyMinutes(name = R.string.thirty_minutes, value = LocalTime.now().plusMinutes(30)),
	ReminderTime.OneHour(name = R.string.one_hour, value = LocalTime.now().plusHours(1)),
	ReminderTime.CustomTime(name = R.string.select_time, value = null)
)
