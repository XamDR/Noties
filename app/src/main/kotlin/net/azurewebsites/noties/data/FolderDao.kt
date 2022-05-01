package net.azurewebsites.noties.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.azurewebsites.noties.domain.FolderEntity

@Dao
interface FolderDao {

	@Insert
	suspend fun insertFolder(folder: FolderEntity)

	@Query("SELECT * FROM Directories ORDER BY (id <> 1), name")
	fun getFolders(): Flow<List<FolderEntity>>

	@Update
	suspend fun updateFolder(folder: FolderEntity)

	@Delete
	suspend fun deleteFolders(folders: List<FolderEntity>)
}