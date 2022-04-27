package net.azurewebsites.eznotes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.azurewebsites.eznotes.core.MediaItemEntity
import net.azurewebsites.eznotes.core.Note

@Dao
abstract class MediaItemDao(private val appDatabase: AppDatabase) {

	@Insert
	abstract suspend fun insertMediaItems(mediaItems: List<MediaItemEntity>)

	@Query("SELECT * FROM MediaItems WHERE note_id =:noteId")
	abstract fun getMediaItems(noteId: Int): Flow<List<MediaItemEntity>>

	@Update
	abstract suspend fun updateMediaItem(mediaItem: MediaItemEntity)

	@Delete
	abstract suspend fun deleteMediaItem(mediaItem: MediaItemEntity)

	@Delete
	abstract suspend fun deleteMediaItems(mediaItems: List<MediaItemEntity>)

	@Transaction
	open suspend fun insertNoteWithMediaItems(directoryId: Int, note: Note, mediaItems: List<MediaItemEntity>) {
		val noteId = appDatabase.noteDao().insertNoteAndUpdateNoteCount(directoryId, note.entity)
		for (mediaItem in mediaItems) {
			mediaItem.noteId = noteId
		}
		insertMediaItems(mediaItems)
	}

	@Transaction
	@Query("SELECT * FROM Notes WHERE directory_id =:directoryId")
	abstract fun getNoteWithMediaItems(directoryId: Int): Flow<List<Note>>

	@Transaction
	open suspend fun updateNote(note: Note) {
		appDatabase.noteDao().updateNote(note.entity)
		insertMediaItems(note.mediaItems.filter { it.id == 0 })
	}

	@Transaction
	open suspend fun deleteNotes(directoryId: Int, notes: List<Note>) {
		for (note in notes) {
			deleteMediaItems(note.mediaItems)
		}
		appDatabase.noteDao().deleteNotesAndUpdateNoteCount(directoryId, notes.map { it.entity })
	}
}