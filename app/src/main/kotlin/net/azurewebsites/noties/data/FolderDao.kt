package net.azurewebsites.noties.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.azurewebsites.noties.core.Folder
import net.azurewebsites.noties.core.FolderEntity

@Dao
abstract class FolderDao(private val appDatabase: AppDatabase) {

	@Insert
	abstract suspend fun insertFolder(folder: FolderEntity)

	@Transaction
	@Query("SELECT * FROM Directories ORDER BY (id <> 1), name")
	abstract fun getFolders(): Flow<List<Folder>>

	@Update
	abstract suspend fun updateFolder(folder: FolderEntity)

	@Delete
	abstract suspend fun deleteFolders(folders: List<FolderEntity>)

	@Transaction
	open suspend fun deleteFolderAndNotes(folder: Folder) {
		for (note in folder.notes) {
			appDatabase.noteDao().moveNotesToTrash(note.folderId)
		}
		deleteFolders(listOf(folder.entity))
	}
}