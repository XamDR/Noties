package net.azurewebsites.noties.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.azurewebsites.noties.core.Notebook
import net.azurewebsites.noties.core.NotebookEntity

@Dao
interface NotebookDao {

	@Insert
	suspend fun insertNotebook(notebook: NotebookEntity)

	@Transaction
	@Query("SELECT * FROM Notebooks WHERE id <> -1 ORDER BY name")
	fun getNotebooks(): Flow<List<Notebook>>

	@Update
	suspend fun updateNotebook(notebook: NotebookEntity)

	@Delete
	suspend fun deleteNotebooks(notebooks: List<NotebookEntity>)

	@Query("UPDATE Notebooks SET note_count = note_count + 1 WHERE id = :notebookId")
	suspend fun incrementNotebookNoteCount(notebookId: Int)

	@Query("UPDATE Notebooks SET note_count = note_count - 1 WHERE id = :notebookId")
	suspend fun decrementNotebookNoteCount(notebookId: Int)
}