package io.github.xamdr.noties.ui.reminders

import java.time.LocalDate
import java.time.LocalTime

sealed class ReminderDate(val name: String, val value: LocalDate?) {
	class Today(name: String, value: LocalDate): ReminderDate(name, value)
	class Tomorrow(name: String, value: LocalDate): ReminderDate(name, value)
	class NextWeek(name: String, value: LocalDate): ReminderDate(name, value)
	class CustomDate(name: String, value: LocalDate?): ReminderDate(name, value)
}

sealed class ReminderTime(val name: String, val value: LocalTime?) {
	class FiveMinutes(name: String, value: LocalTime): ReminderTime(name, value)
	class TenMinutes(name: String, value: LocalTime): ReminderTime(name, value)
	class FifteenMinutes(name: String, value: LocalTime): ReminderTime(name, value)
	class ThirtyMinutes(name: String, value: LocalTime): ReminderTime(name, value)
	class OneHour(name: String, value: LocalTime): ReminderTime(name, value)
	class CustomTime(name: String, value: LocalTime?): ReminderTime(name, value)
}