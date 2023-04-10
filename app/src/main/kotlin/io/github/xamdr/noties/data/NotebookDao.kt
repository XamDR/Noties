package io.github.xamdr.noties.data

import androidx.room.*
import io.github.xamdr.noties.data.entity.NotebookEntityCrossRefLocal
import io.github.xamdr.noties.data.entity.NotebookEntityLocal
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookDao {

	@Insert
	suspend fun insertNotebook(notebook: NotebookEntityLocal)

	@Transaction
	@Query("SELECT * FROM Notebooks ORDER BY (id <> 1), name")
	fun getNotebooks(): Flow<List<NotebookEntityCrossRefLocal>>

	@Query("SELECT * FROM Notebooks ORDER BY name")
	suspend fun getNotebooksAsyc(): List<NotebookEntityLocal>

	@Update
	suspend fun updateNotebook(notebook: NotebookEntityLocal)

	@Delete
	suspend fun deleteNotebooks(notebooks: List<NotebookEntityLocal>)

	@Query("UPDATE Notebooks SET note_count = note_count + 1 WHERE id = :notebookId")
	suspend fun incrementNotebookNoteCount(notebookId: Int)

	@Query("UPDATE Notebooks SET note_count = note_count - 1 WHERE id = :notebookId")
	suspend fun decrementNotebookNoteCount(notebookId: Int)

	@Query("SELECT EXISTS (SELECT id FROM Notebooks WHERE id = :id LIMIT 1)")
	suspend fun getIfNotebookIdExists(id: Int): Boolean
}