package io.github.xamdr.noties.data.dao

import androidx.room.*
import io.github.xamdr.noties.data.entity.image.DatabaseImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

	@Insert
	suspend fun insertImages(images: List<DatabaseImageEntity>)

	@Query("SELECT * FROM Images WHERE note_id =:noteId")
	fun getImages(noteId: Int): Flow<List<DatabaseImageEntity>>

	@Update
	suspend fun updateImage(image: DatabaseImageEntity)

	@Delete
	suspend fun deleteImages(images: List<DatabaseImageEntity>)

	@Query("DELETE FROM Images WHERE note_id IN (SELECT id FROM Notes WHERE is_trashed = 1)")
	suspend fun deleteImagesForTrashedNotes()
}