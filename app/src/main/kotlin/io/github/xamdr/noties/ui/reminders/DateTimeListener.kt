package io.github.xamdr.noties.ui.reminders

import java.time.Instant

interface DateTimeListener {
	fun onReminderDateSet(dateTime: Instant)
	fun onReminderDateDeleted()
}