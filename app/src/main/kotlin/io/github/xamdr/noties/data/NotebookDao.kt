package io.github.xamdr.noties.data

import androidx.room.*
import io.github.xamdr.noties.core.Notebook
import io.github.xamdr.noties.core.NotebookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookDao {

	@Insert
	suspend fun insertNotebook(notebook: NotebookEntity)

	@Transaction
	@Query("SELECT * FROM Notebooks ORDER BY (id <> 1), name")
	fun getNotebooks(): Flow<List<Notebook>>

	@Query("SELECT * FROM Notebooks ORDER BY name")
	suspend fun getNotebooksAsyc(): List<NotebookEntity>

	@Update
	suspend fun updateNotebook(notebook: NotebookEntity)

	@Delete
	suspend fun deleteNotebooks(notebooks: List<NotebookEntity>)

	@Query("UPDATE Notebooks SET note_count = note_count + 1 WHERE id = :notebookId")
	suspend fun incrementNotebookNoteCount(notebookId: Int)

	@Query("UPDATE Notebooks SET note_count = note_count - 1 WHERE id = :notebookId")
	suspend fun decrementNotebookNoteCount(notebookId: Int)

	@Query("SELECT EXISTS (SELECT id FROM Notebooks WHERE id = :id LIMIT 1)")
	suspend fun getIfNotebookIdExists(id: Int): Boolean
}