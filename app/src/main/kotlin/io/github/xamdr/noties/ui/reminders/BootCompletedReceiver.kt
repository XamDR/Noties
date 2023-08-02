package io.github.xamdr.noties.ui.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.data.repository.NoteRepository
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

	@Inject lateinit var noteRepository: NoteRepository
	@Inject lateinit var preferenceStorage: PreferenceStorage

	override fun onReceive(context: Context, intent: Intent?) {
		if (intent != null && intent.action == Intent.ACTION_BOOT_COMPLETED) {
			setAlarms(noteRepository, context)
		}
	}

	private fun setAlarms(noteRepository: NoteRepository, context: Context) {
		CoroutineScope(Dispatchers.Main).launch {
			val notesWithReminders = noteRepository.getNotesWithReminder().map { it.asDomainModel() }
			for (note in notesWithReminders) {
				AlarmManagerHelper.setAlarm(context, note, preferenceStorage.isExactAlarmEnabled)
			}
		}
	}
}