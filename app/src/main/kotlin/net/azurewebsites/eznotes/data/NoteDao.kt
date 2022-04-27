package net.azurewebsites.eznotes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.azurewebsites.eznotes.core.DirectoryEntity
import net.azurewebsites.eznotes.core.NoteEntity

@Dao
abstract class NoteDao(private val appDatabase: AppDatabase) {

	@Insert
	abstract suspend fun insertNote(note: NoteEntity): Long

	@Query("SELECT * FROM Notes WHERE text LIKE '%' || :contentToSearch || '%'")
	abstract fun getNotesByContent(contentToSearch: String): Flow<List<NoteEntity>>

	@Update
	abstract suspend fun updateNote(note: NoteEntity)

	@Delete
	abstract suspend fun deleteNotes(notes: List<NoteEntity>)

	@Query("UPDATE Directories SET note_count = note_count + 1 WHERE id = :directoryId")
	abstract suspend fun incrementNoteCount(directoryId: Int)

	@Query("UPDATE Directories SET note_count = note_count - 1 WHERE id = :directoryId")
	abstract suspend fun decrementNoteCount(directoryId: Int)

	@Transaction
	open suspend fun insertNoteAndUpdateNoteCount(directoryId: Int, note: NoteEntity): Long {
		incrementNoteCount(directoryId)
		return insertNote(note)
	}

	@Transaction
	open suspend fun deleteNotesAndUpdateNoteCount(directoryId: Int, notes: List<NoteEntity>) {
		deleteNotes(notes)
		decrementNoteCount(directoryId)
	}

	@Query("DELETE FROM Notes WHERE directory_id = :directoryId")
	abstract suspend fun deleteNotesByDirectory(directoryId: Int)

	@Transaction
	open suspend fun deleteDirectoriesAndNotes(directories: List<DirectoryEntity>) {
		for (directory in directories) {
			deleteNotesByDirectory(directory.id)
		}
		appDatabase.directoryDao().deleteDirectories(directories)
	}
}