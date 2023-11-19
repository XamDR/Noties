package io.github.xamdr.noties.ui.reminders

sealed interface ReminderDateState {
	data object ReminderDateNotSet : ReminderDateState
	data object ReminderDateValid : ReminderDateState
	data object ReminderDateInvalid : ReminderDateState
}