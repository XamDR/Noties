package io.github.xamdr.noties.ui.reminders

import android.content.Context
import io.github.xamdr.noties.R
import java.time.LocalDate
import java.time.LocalTime

class DateTimePickerHelper(context: Context) {

	val dates = mutableListOf(
		ReminderDate.Today(name = context.getString(R.string.today), value = today()),
		ReminderDate.Tomorrow(name = context.getString(R.string.tomorrow), value = tomorrow()),
		ReminderDate.NextWeek(name = context.getString(R.string.next_week), value = nextWeek()),
		ReminderDate.CustomDate(name = context.getString(R.string.select_date), value = null)
	)

	val times = mutableListOf(
		ReminderTime.FiveMinutes(name = context.getString(R.string.five_minutes), value = addFiveMinutes()),
		ReminderTime.TenMinutes(name = context.getString(R.string.ten_minutes), value = addTenMinutes()),
		ReminderTime.FifteenMinutes(name = context.getString(R.string.fifteen_minutes), value = addFifteenMinutes()),
		ReminderTime.ThirtyMinutes(name = context.getString(R.string.thirty_minutes), value = addThirtyMinutes()),
		ReminderTime.OneHour(name = context.getString(R.string.one_hour), value = addOneHour()),
		ReminderTime.CustomTime(name = context.getString(R.string.select_time), value = null)
	)

	private fun today() = LocalDate.now()

	private fun tomorrow() = LocalDate.now().plusDays(1)

	private fun nextWeek() = LocalDate.now().plusDays(7)

	private fun addFiveMinutes() = LocalTime.now().plusMinutes(5)

	private fun addTenMinutes() = LocalTime.now().plusMinutes(10)

	private fun addFifteenMinutes() = LocalTime.now().plusMinutes(15)

	private fun addThirtyMinutes() = LocalTime.now().plusMinutes(30)

	private fun addOneHour() = LocalTime.now().plusHours(1)
}