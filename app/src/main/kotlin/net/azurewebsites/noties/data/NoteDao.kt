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
	@Query("SELECT * FROM Notes WHERE folder_id = -1")
	fun getTrashedNotes(): Flow<List<Note>>

	@Update
	suspend fun updateNote(note: NoteEntity)

	@Delete
	suspend fun deleteNotes(notes: List<NoteEntity>)

	// This will be deleted

	@Query("UPDATE Directories SET note_count = note_count + 1 WHERE id = :directoryId")
	suspend fun incrementNoteCount(directoryId: Int)

	@Query("UPDATE Directories SET note_count = note_count - 1 WHERE id = :directoryId")
	suspend fun decrementNoteCount(directoryId: Int)

	@Transaction
	suspend fun insertNoteAndUpdateNoteCount(directoryId: Int, note: NoteEntity): Long {
		incrementNoteCount(directoryId)
		return insertNote(note)
	}

	@Transaction
	suspend fun deleteNotesAndUpdateNoteCount(directoryId: Int, notes: List<NoteEntity>) {
		deleteNotes(notes)
		decrementNoteCount(directoryId)
	}

	// This won't be deleted

	@Query("DELETE FROM Notes WHERE folder_id = -1")
	suspend fun emptyTrash(): Int
}