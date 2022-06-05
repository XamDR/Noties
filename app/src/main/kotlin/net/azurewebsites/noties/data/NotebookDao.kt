package net.azurewebsites.noties.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.azurewebsites.noties.core.Notebook
import net.azurewebsites.noties.core.NotebookEntity

@Dao
interface NotebookDao {

	@Insert
	suspend fun insertFolder(notebook: NotebookEntity)

	@Transaction
	@Query("SELECT * FROM Notebooks WHERE id <> -1 ORDER BY (id <> 1), name")
	fun getFolders(): Flow<List<Notebook>>

	@Update
	suspend fun updateFolder(notebook: NotebookEntity)

	@Delete
	suspend fun deleteFolders(notebooks: List<NotebookEntity>)

	@Query("UPDATE Notebooks SET note_count = note_count + 1 WHERE id = :notebookId")
	suspend fun incrementNoteCount(notebookId: Int)

	@Query("UPDATE Notebooks SET note_count = note_count - 1 WHERE id = :notebookId")
	suspend fun decrementNoteCount(notebookId: Int)
}