package io.github.xamdr.noties.ui.reminders

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.theme.NotiesTheme
import java.time.Instant

@AndroidEntryPoint
class DateTimePickerActivity : FragmentActivity() {

	private val viewModel by viewModels<DateTimePickerViewModel>()
	private val noteId by lazy(LazyThreadSafetyMode.NONE) {
		intent.getLongExtra(Constants.BUNDLE_NOTE_ID, 0L)
	}
	private val reminderDate by lazy(LazyThreadSafetyMode.NONE) {
		intent.getLongExtra(Constants.BUNDLE_REMINDER_DATE, 0L)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent { DateTimePickerActivityContent() }
	}

	@Composable
	private fun DateTimePickerActivityContent() {
		NotiesTheme {
			DateTimePickerDialog(
				reminderDate = reminderDate,
				onReminderDateSet = { dateTime -> updateReminder(noteId, dateTime) },
				onCancelReminder = { deleteReminder(noteId) },
				onDismiss = { finish() },
				viewModel = viewModel
			)
		}
	}

	private fun updateReminder(noteId: Long, dateTime: Instant) {
		viewModel.updateReminder(noteId = noteId, dateTime = dateTime) { note ->
			AlarmManagerHelper.setAlarm(context = this, note = note)
			finish()
		}
	}

	private fun deleteReminder(noteId: Long) {
		viewModel.updateReminder(noteId = noteId, dateTime = null) {
			finish()
		}
	}
}