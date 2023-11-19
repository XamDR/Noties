package io.github.xamdr.noties.ui.reminders

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.DateTimeHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class DateTimePickerViewModel @Inject constructor() : ViewModel() {

	private val reminderDateState: MutableStateFlow<ReminderDateState> = MutableStateFlow(ReminderDateState.ReminderDateNotSet)
	val reminderState = reminderDateState.asStateFlow()

	fun onReminderDateSelected(date: String, time: String) {
		if (date == String.Empty || time == String.Empty) {
			reminderDateState.update { ReminderDateState.ReminderDateNotSet }
		}
		else {
			val localDate = LocalDate.parse(date, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
			val localTime = LocalTime.parse(time, DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
			if (DateTimeHelper.isPast(localDate, localTime)) {
				reminderDateState.update { ReminderDateState.ReminderDateInvalid }
			}
			else {
				reminderDateState.update { ReminderDateState.ReminderDateValid }
			}
		}
	}

	fun onDateSet(selection: Long): ReminderDate {
		val selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
		return ReminderDate.CustomDate(R.string.select_date, selectedDate)
	}

	fun onTimeSet(hour: Int, minute: Int): ReminderTime {
		val selectedTime = LocalTime.of(hour, minute)
		return ReminderTime.CustomTime(R.string.select_time, selectedTime)
	}

	fun onDateTimeSet(selectedDateText: String, selectedTimeText: String): Instant {
		val selectedDate = LocalDate.parse(selectedDateText, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
		val selectedTime = LocalTime.parse(selectedTimeText, DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
		return LocalDateTime.of(selectedDate, selectedTime).atZone(ZoneId.systemDefault()).toInstant()
	}
}