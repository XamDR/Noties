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
import javax.inject.Inject

@HiltViewModel
class DateTimePickerViewModel @Inject constructor(
	private val updateReminderUseCase: UpdateReminderUseCase,
	private val getNoteByIdUseCase: GetNoteByIdUseCase
) : ViewModel() {

	private val reminderDateState: MutableStateFlow<ReminderDateState> = MutableStateFlow(ReminderDateState.ReminderDateNotSet)
	val reminderState = reminderDateState.asStateFlow()

	fun onReminderDateSelected(date: LocalDate?, time: LocalTime?) {
		if (date == null || time == null) {
			reminderDateState.update { ReminderDateState.ReminderDateNotSet }
		}
		else {
			if (DateTimeHelper.isPast(date, time)) {
				reminderDateState.update { ReminderDateState.ReminderDateInvalid }
			}
			else {
				reminderDateState.update { ReminderDateState.ReminderDateValid }
			}
		}
	}

	fun onDateSet(selection: Long): LocalDate {
		return Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
	}

	fun onTimeSet(hour: Int, minute: Int): LocalTime {
		return LocalTime.of(hour, minute)
	}

	fun onDateTimeSet(date: LocalDate?, time: LocalTime?): Instant {
		val selectedDate = date ?: throw IllegalArgumentException("date is null")
		val selectedTime = time ?: throw IllegalArgumentException("time is null")
		return LocalDateTime.of(selectedDate, selectedTime).atZone(ZoneId.systemDefault()).toInstant()
	}

	fun updateReminder(noteId: Long, reminder: Instant?, onReminderUpdated: (Note) -> Unit) {
		viewModelScope.launch {
			updateReminderUseCase(noteId, reminder)
			val updatedNote = getNoteByIdUseCase(noteId)
			onReminderUpdated(updatedNote)
		}
	}
}