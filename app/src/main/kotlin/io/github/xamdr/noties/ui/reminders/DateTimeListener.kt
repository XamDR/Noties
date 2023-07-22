package io.github.xamdr.noties.ui.reminders

import java.time.Instant

interface DateTimeListener {
	fun onDateTimeSet(dateTime: Instant)
}