package net.azurewebsites.noties.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.azurewebsites.noties.core.FolderEntity
import net.azurewebsites.noties.core.NoteEntity

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

	@Query("DELETE FROM Notes WHERE folder_id = :directoryId")
	abstract suspend fun deleteNotesByDirectory(directoryId: Int)

	@Transaction
	open suspend fun deleteDirectoriesAndNotes(folders: List<FolderEntity>) {
		for (directory in folders) {
			deleteNotesByDirectory(directory.id)
		}
		appDatabase.folderDao().deleteFolders(folders)
	}

	@Query("UPDATE Notes SET folder_id = -1 WHERE folder_id = :folderId")
	abstract fun moveNotesToTrash(folderId: Int)
}