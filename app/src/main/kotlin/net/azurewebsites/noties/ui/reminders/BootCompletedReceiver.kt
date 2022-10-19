package net.azurewebsites.noties.ui.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.azurewebsites.noties.data.NoteDao
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

	private val coroutineScope = CoroutineScope(Dispatchers.Default)
	@Inject lateinit var noteDao: NoteDao

	override fun onReceive(context: Context, intent: Intent?) {
		if (intent != null && intent.action == Intent.ACTION_BOOT_COMPLETED) {
			setAlarms(noteDao, context)
		}
	}

	private fun setAlarms(noteDao: NoteDao, context: Context) {
		coroutineScope.launch {
			val notes = noteDao.getNotesWithReminderAsync()
			withContext(Dispatchers.Main) {
				for (note in notes) {
					val reminderDate = note.entity.reminderDate
					if (reminderDate != null) {
						val delay = reminderDate.toInstant().toEpochMilli()
						AlarmManagerHelper.setAlarmManager(context, delay, note)
					}
				}
			}
		}
	}
}