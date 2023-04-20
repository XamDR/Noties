package io.github.xamdr.noties.data.repository

import io.github.xamdr.noties.data.dao.NoteDao
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

	suspend fun insertNote(note: DatabaseNoteEntity): Long {
		return noteDao.insertNote(note)
	}

	fun getNotesByTag(tagName: String): Flow<List<DatabaseNoteEntity>> {
		return noteDao.getNotesByTag(tagName)
	}

	fun getAllNotes(): Flow<List<DatabaseNoteEntity>> {
		return noteDao.getAllNotes()
	}
}