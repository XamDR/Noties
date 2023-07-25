package io.github.xamdr.noties.ui.reminders

sealed interface ReminderDateState {
	object ReminderDateNotSet : ReminderDateState
	object ReminderDateValid : ReminderDateState
	object ReminderDateInvalid : ReminderDateState
}