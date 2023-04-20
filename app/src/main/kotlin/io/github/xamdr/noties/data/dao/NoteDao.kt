package io.github.xamdr.noties.data.dao

import androidx.room.*
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

	@Insert
	suspend fun insertNote(note: DatabaseNoteEntity): Long

	@Transaction
	@Query("SELECT * FROM Notes WHERE instr(tags, :tagName) AND is_trashed <> 1")
	fun getNotesByTag(tagName: String): Flow<List<DatabaseNoteEntity>>

	@Transaction
	@Query("SELECT * FROM Notes WHERE is_trashed <> 1")
	fun getAllNotes(): Flow<List<DatabaseNoteEntity>>

	@Update
	suspend fun updateNote(note: DatabaseNoteEntity)

	@Delete
	suspend fun deleteNote(note: DatabaseNoteEntity)

	@Transaction
	@Query("SELECT * FROM Notes WHERE is_trashed = 1")
	fun getTrashedNotes(): Flow<List<DatabaseNoteEntity>>

	@Transaction
	@Query("SELECT * FROM Notes WHERE is_trashed = 1")
	suspend fun getTrashedNotesSync(): List<DatabaseNoteEntity>

	@Query("DELETE FROM Notes WHERE is_trashed = 1")
	suspend fun deleteTrashedNotes(): Int

	@Transaction
	@Query("SELECT * FROM notes WHERE reminder_date IS NOT NULL")
	suspend fun getNotesWithReminderAsync(): List<DatabaseNoteEntity>
}