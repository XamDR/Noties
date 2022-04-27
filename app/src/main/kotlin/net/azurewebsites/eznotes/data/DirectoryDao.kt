package net.azurewebsites.eznotes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.azurewebsites.eznotes.core.DirectoryEntity

@Dao
interface DirectoryDao {

	@Insert
	suspend fun insertDirectory(directory: DirectoryEntity)

	@Query("SELECT * FROM Directories ORDER BY (id <> 1), name")
	fun getDirectories(): Flow<List<DirectoryEntity>>

	@Update
	suspend fun updateDirectory(directory: DirectoryEntity)

	@Delete
	suspend fun deleteDirectories(directories: List<DirectoryEntity>)
}