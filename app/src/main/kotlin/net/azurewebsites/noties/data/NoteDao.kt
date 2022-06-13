package net.azurewebsites.noties.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.core.NoteEntity

@Dao
interface NoteDao {

	@Insert
	suspend fun insertNote(note: NoteEntity): Long

	@Transaction
	@Query("SELECT * FROM Notes WHERE notebook_id = :notebookId")
	fun getNotes(notebookId: Int): Flow<List<Note>>

	@Transaction
	@Query("SELECT * FROM Notes WHERE notebook_id <> -1")
	fun getAllNotes(): Flow<List<Note>>

	@Update
	suspend fun updateNote(note: NoteEntity)

	@Delete
	suspend fun deleteNote(note: NoteEntity)

	@Transaction
	@Query("SELECT * FROM Notes WHERE notebook_id = -1")
	fun getTrashedNotes(): Flow<List<Note>>

	@Query("DELETE FROM Notes WHERE notebook_id = -1")
	suspend fun emptyTrash(): Int
}