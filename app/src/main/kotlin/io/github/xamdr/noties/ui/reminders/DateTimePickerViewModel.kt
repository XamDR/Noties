package io.github.xamdr.noties.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class DateTimePickerViewModel @Inject constructor() : ViewModel() {

	private val reminderDateState: MutableStateFlow<ReminderDateState> = MutableStateFlow(ReminderDateState.ReminderDateNotSet)
	val reminderState = reminderDateState.asLiveData()

	fun onReminderDateSelected(date: ReminderDate?, time: ReminderTime?) {
		if (date?.value == null || time?.value == null) {
			reminderDateState.update { ReminderDateState.ReminderDateNotSet }
		}
		else {
			if (date.value.isBefore(LocalDate.now()) || time.value.isBefore(LocalTime.now())) {
				reminderDateState.update { ReminderDateState.ReminderDateInvalid }
			}
			else {
				reminderDateState.update { ReminderDateState.ReminderDateValid }
			}
		}
	}
}