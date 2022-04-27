package net.azurewebsites.noties.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.azurewebsites.noties.domain.FolderEntity

@Dao
interface FolderDao {

	@Insert
	suspend fun insertDirectory(folder: FolderEntity)

	@Query("SELECT * FROM Directories ORDER BY (id <> 1), name")
	fun getDirectories(): Flow<List<FolderEntity>>

	@Update
	suspend fun updateDirectory(folder: FolderEntity)

	@Delete
	suspend fun deleteDirectories(folders: List<FolderEntity>)
}