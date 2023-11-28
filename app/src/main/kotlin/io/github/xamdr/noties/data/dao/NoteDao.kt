package io.github.xamdr.noties.data.dao

import androidx.room.*
import io.github.xamdr.noties.data.entity.media.DatabaseMediaItemEntity
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

	@Insert
	suspend fun insertNote(note: DatabaseNoteEntity): Long

	@Query("SELECT * FROM Notes " +
			"LEFT JOIN MediaItems ON Notes.id = MediaItems.note_id " +
			"WHERE instr(Notes.tags, :tagName) AND Notes.trashed <> 1 " +
			"ORDER BY Notes.id DESC")
	fun getNotesByTag(tagName: String): Flow<Map<DatabaseNoteEntity, List<DatabaseMediaItemEntity>>>

	@Query("SELECT * FROM Notes " +
			"LEFT JOIN MediaItems ON Notes.id = MediaItems.note_id " +
			"WHERE Notes.id = :noteId")
	suspend fun getNoteById(noteId: Long): Map<DatabaseNoteEntity, List<DatabaseMediaItemEntity>>

	@Query("SELECT * FROM Notes " +
			"LEFT JOIN MediaItems ON Notes.id = MediaItems.note_id " +
			"WHERE Notes.trashed <> 1 AND Notes.archived <> 1 " +
			"ORDER BY Notes.id DESC")
	fun getAllNotes(): Flow<Map<DatabaseNoteEntity, List<DatabaseMediaItemEntity>>>

	@Update
	suspend fun updateNote(note: DatabaseNoteEntity)

	@Delete
	suspend fun deleteNote(note: DatabaseNoteEntity)

	@Query("SELECT * FROM Notes " +
			"LEFT JOIN MediaItems ON Notes.id = MediaItems.note_id " +
			"WHERE Notes.trashed = 1 " +
			"ORDER BY Notes.id DESC")
	fun getTrashedNotes(): Flow<Map<DatabaseNoteEntity, List<DatabaseMediaItemEntity>>>

	@Query("DELETE FROM Notes WHERE Notes.trashed = 1")
	suspend fun deleteTrashedNotes(): Int

	@Query("SELECT * FROM Notes " +
			"LEFT JOIN MediaItems ON Notes.id = MediaItems.note_id " +
			"WHERE Notes.archived = 1 " +
			"ORDER BY Notes.id DESC")
	fun getArchivedNotes(): Flow<Map<DatabaseNoteEntity, List<DatabaseMediaItemEntity>>>

	@Query("SELECT * FROM Notes " +
			"LEFT JOIN MediaItems ON Notes.id = MediaItems.note_id " +
			"WHERE reminder_date IS NOT NULL AND Notes.trashed <> 1 " +
			"ORDER BY Notes.id DESC")
	fun getNotesWithReminder(): Flow<Map<DatabaseNoteEntity, List<DatabaseMediaItemEntity>>>

	@Query("SELECT * FROM Notes " +
			"LEFT JOIN MediaItems ON Notes.id = MediaItems.note_id " +
			"WHERE reminder_date IS NOT NULL AND reminder_date > CURRENT_TIMESTAMP AND Notes.trashed <> 1 " +
			"ORDER BY Notes.id DESC")
	fun getNotesWithReminderPastCurrentTime(): Flow<Map<DatabaseNoteEntity, List<DatabaseMediaItemEntity>>>

	@Query("UPDATE Notes SET reminder_date = :reminderDate WHERE id = :noteId")
	suspend fun updateReminderForNote(noteId: Long, reminderDate: Long?)

	@Query("UPDATE NOTES SET tags = REPLACE(tags, :oldTag, :newTag)")
	suspend fun updateTagForNote(newTag: String, oldTag: String)
}