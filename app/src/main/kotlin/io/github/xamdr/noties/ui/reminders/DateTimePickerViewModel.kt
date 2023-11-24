package io.github.xamdr.noties.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.usecase.GetNoteByIdUseCase
import io.github.xamdr.noties.domain.usecase.UpdateReminderUseCase
import io.github.xamdr.noties.ui.helpers.DateTimeHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class DateTimePickerViewModel @Inject constructor(
	private val updateReminderUseCase: UpdateReminderUseCase,
	private val getNoteByIdUseCase: GetNoteByIdUseCase
) : ViewModel() {

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
		return ReminderDate.CustomDate(selectedDate)
	}

	fun onTimeSet(hour: Int, minute: Int): ReminderTime {
		val selectedTime = LocalTime.of(hour, minute)
		return ReminderTime.CustomTime(selectedTime)
	}

	fun onDateTimeSet(selectedDateText: String, selectedTimeText: String): Instant {
		val selectedDate = LocalDate.parse(selectedDateText, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
		val selectedTime = LocalTime.parse(selectedTimeText, DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
		return LocalDateTime.of(selectedDate, selectedTime).atZone(ZoneId.systemDefault()).toInstant()
	}

	fun updateReminder(noteId: Long, dateTime: Instant?, onUpdate: (Note) -> Unit) {
		viewModelScope.launch {
			updateReminderUseCase(noteId, dateTime)
			val updatedNote = getNoteByIdUseCase(noteId)
			onUpdate(updatedNote)
		}
	}
}