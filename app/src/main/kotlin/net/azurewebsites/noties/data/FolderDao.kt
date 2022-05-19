package net.azurewebsites.noties.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.azurewebsites.noties.core.Folder
import net.azurewebsites.noties.core.FolderEntity

@Dao
interface FolderDao {

	@Insert
	suspend fun insertFolder(folder: FolderEntity)

	@Transaction
	@Query("SELECT * FROM Directories WHERE id <> -1 ORDER BY (id <> 1), name")
	fun getFolders(): Flow<List<Folder>>

	@Update
	suspend fun updateFolder(folder: FolderEntity)

	@Delete
	suspend fun deleteFolders(folders: List<FolderEntity>)

	@Query("UPDATE Directories SET note_count = note_count + 1 WHERE id = :folderId")
	suspend fun incrementNoteCount(folderId: Int)
}