package io.github.xamdr.noties.data

import androidx.room.*
import io.github.xamdr.noties.core.Note
import io.github.xamdr.noties.core.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

	@Insert
	suspend fun insertNote(note: NoteEntity): Long

	@Transaction
	@Query("SELECT * FROM Notes WHERE notebook_id = :notebookId AND is_trashed <> 1")
	fun getNotes(notebookId: Int): Flow<List<Note>>

	@Transaction
	@Query("SELECT * FROM Notes WHERE is_trashed <> 1")
	fun getAllNotes(): Flow<List<Note>>

	@Update
	suspend fun updateNote(note: NoteEntity)

	@Delete
	suspend fun deleteNote(note: NoteEntity)

	@Transaction
	@Query("SELECT * FROM Notes WHERE is_trashed = 1")
	fun getTrashedNotes(): Flow<List<Note>>

	@Transaction
	@Query("SELECT * FROM Notes WHERE is_trashed = 1")
	suspend fun getTrashedNotesSync(): List<Note>

	@Query("DELETE FROM Notes WHERE is_trashed = 1")
	suspend fun deleteTrashedNotes(): Int

	@Transaction
	@Query("SELECT * FROM notes WHERE reminder_date IS NOT NULL")
	suspend fun getNotesWithReminderAsync(): List<Note>
}