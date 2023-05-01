package io.github.xamdr.noties.data.dao

import androidx.room.*
import io.github.xamdr.noties.data.entity.image.DatabaseImageEntity
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

	@Insert
	suspend fun insertNote(note: DatabaseNoteEntity): Long

	@Query("SELECT * FROM Notes " +
			"JOIN Images ON Notes.id = Images.note_id " +
			"WHERE instr(Notes.tags, :tagName) AND Notes.is_trashed <> 1 " +
			"ORDER BY Notes.id DESC")
	fun getNotesByTag(tagName: String): Flow<Map<DatabaseNoteEntity, List<DatabaseImageEntity>>>

	@Query("SELECT * FROM Notes " +
			"JOIN Images ON Notes.id = Images.note_id " +
			"WHERE Notes.id = :noteId")
	suspend fun getNoteById(noteId: Long): Map<DatabaseNoteEntity, List<DatabaseImageEntity>>

	@Query("SELECT * FROM Notes " +
			"JOIN Images ON Notes.id = Images.note_id " +
			"WHERE Notes.is_trashed <> 1 " +
			"ORDER BY Notes.id DESC")
	fun getAllNotes(): Flow<Map<DatabaseNoteEntity, List<DatabaseImageEntity>>>

	@Update
	suspend fun updateNote(note: DatabaseNoteEntity)

	@Delete
	suspend fun deleteNotes(notes: List<DatabaseNoteEntity>): Int

	@Query("SELECT * FROM Notes " +
			"JOIN Images ON Notes.id = Images.note_id " +
			"WHERE Notes.is_trashed = 1 " +
			"ORDER BY Notes.id DESC")
	fun getTrashedNotes(): Flow<Map<DatabaseNoteEntity, List<DatabaseImageEntity>>>
}