package io.github.xamdr.noties.ui.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.domain.usecase.GetNotesWithReminderUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

//	@Inject lateinit var noteRepository: NoteRepository
	@Inject lateinit var notesWithReminderUseCase: GetNotesWithReminderUseCase

	override fun onReceive(context: Context, intent: Intent?) {
		if (intent != null && intent.action == Intent.ACTION_BOOT_COMPLETED) {
			setAlarms(context)
		}
	}

	private fun setAlarms(context: Context) {
		CoroutineScope(Dispatchers.Main).launch {
			val noteWithReminders = notesWithReminderUseCase()
			noteWithReminders.collect { list ->
				for (note in list) {
					AlarmManagerHelper.setAlarm(context, note)
				}
			}
		}
	}
}