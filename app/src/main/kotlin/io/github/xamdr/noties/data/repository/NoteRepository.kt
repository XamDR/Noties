package io.github.xamdr.noties.data.repository

import io.github.xamdr.noties.data.dao.NoteDao
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

	suspend fun insertNote(note: DatabaseNoteEntity): Long {
		return noteDao.insertNote(note)
	}

	suspend fun getNoteById(noteId: Long): DatabaseNoteEntity {
		return noteDao.getNoteById(noteId)
	}

	fun getNotesByTag(tagName: String): Flow<List<DatabaseNoteEntity>> {
		return noteDao.getNotesByTag(tagName)
	}

	fun getAllNotes(): Flow<List<DatabaseNoteEntity>> {
		return noteDao.getAllNotes()
	}

	suspend fun updateNote(note: DatabaseNoteEntity) {
		noteDao.updateNote(note)
	}

	suspend fun deleteNote(note: DatabaseNoteEntity) {
		noteDao.deleteNote(note)
	}

	fun getTrashedNotes(): Flow<List<DatabaseNoteEntity>> {
		return noteDao.getTrashedNotes()
	}

	suspend fun deleteTrashedNotes(notes: List<DatabaseNoteEntity>): Int {
		return noteDao.deleteNotes(notes)
	}
}