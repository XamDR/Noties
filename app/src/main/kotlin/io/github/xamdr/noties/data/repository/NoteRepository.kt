package io.github.xamdr.noties.data.repository

import io.github.xamdr.noties.data.dao.NoteDao
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity

class NoteRepository(private val noteDao: NoteDao) {

	suspend fun insertNote(note: DatabaseNoteEntity) = noteDao.insertNote(note)

	suspend fun getNoteById(noteId: Long) = noteDao.getNoteById(noteId)

	fun getNotesByTag(tagName: String) = noteDao.getNotesByTag(tagName)

	fun getAllNotes() = noteDao.getAllNotes()

	suspend fun updateNote(note: DatabaseNoteEntity) = noteDao.updateNote(note)

	suspend fun deleteNote(note: DatabaseNoteEntity) = noteDao.deleteNote(note)

	suspend fun deleteNoteById(noteId: Long) = noteDao.deleteNoteById(noteId)

	fun getTrashedNotes() = noteDao.getTrashedNotes()

	fun getArchivedNotes() = noteDao.getArchivedNotes()

	suspend fun deleteTrashedNotes() = noteDao.deleteTrashedNotes()

	fun getNotesWithReminder() = noteDao.getNotesWithReminder()

	fun getNotesWithReminderPastCurrentTime() = noteDao.getNotesWithReminderPastCurrentTime()

	suspend fun updateReminderForNote(noteId: Long, reminderDate: Long?) = noteDao.updateReminderForNote(noteId, reminderDate)

	suspend fun updateTagForNote(newTag: String, oldTag: String) = noteDao.updateTagForNote(newTag, oldTag)
}