package net.azurewebsites.noties.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.azurewebsites.noties.core.ImageEntity

@Dao
interface ImageDao {

	@Insert
	suspend fun insertImages(images: List<ImageEntity>)

	@Query("SELECT * FROM Images WHERE note_id =:noteId")
	fun getImages(noteId: Int): Flow<List<ImageEntity>>

	@Update
	suspend fun updateImage(image: ImageEntity)

	@Delete
	suspend fun deleteImages(images: List<ImageEntity>)

	@Query("DELETE FROM Images WHERE note_id IN (SELECT id FROM Notes WHERE is_trashed = 1)")
	suspend fun deleteImagesForTrashedNotes()
}