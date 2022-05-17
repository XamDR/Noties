package net.azurewebsites.noties.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.core.Note

@Dao
abstract class ImageDao(private val appDatabase: AppDatabase) {

	@Insert
	abstract suspend fun insertImages(images: List<ImageEntity>)

	@Query("SELECT * FROM MediaItems WHERE note_id =:noteId")
	abstract fun getImages(noteId: Int): Flow<List<ImageEntity>>

	@Update
	abstract suspend fun updateImage(image: ImageEntity)

	@Delete
	abstract suspend fun deleteImage(image: ImageEntity)

	@Delete
	abstract suspend fun deleteImages(images: List<ImageEntity>)

	// This will no longer be needed

	@Transaction
	open suspend fun insertNoteWithMediaItems(directoryId: Int, note: Note, images: List<ImageEntity>) {
		val noteId = appDatabase.noteDao().insertNoteAndUpdateNoteCount(directoryId, note.entity)
		for (mediaItem in images) {
			mediaItem.noteId = noteId
		}
		insertImages(images)
	}

	@Transaction
	@Query("SELECT * FROM Notes WHERE folder_id =:directoryId")
	abstract fun getNoteWithMediaItems(directoryId: Int): Flow<List<Note>>

	@Transaction
	open suspend fun updateNote(note: Note) {
		appDatabase.noteDao().updateNote(note.entity)
		insertImages(note.images.filter { it.id == 0 })
	}

	@Transaction
	open suspend fun deleteNotes(directoryId: Int, notes: List<Note>) {
		for (note in notes) {
			deleteImages(note.images)
		}
		appDatabase.noteDao().deleteNotesAndUpdateNoteCount(directoryId, notes.map { it.entity })
	}
}