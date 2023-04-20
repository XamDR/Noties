package io.github.xamdr.noties.data.repository

import io.github.xamdr.noties.data.dao.ImageDao
import io.github.xamdr.noties.data.entity.image.DatabaseImageEntity
import kotlinx.coroutines.flow.Flow

class ImageRepository(private val imageDao: ImageDao) {

	suspend fun insertImages(images: List<DatabaseImageEntity>) {
		imageDao.insertImages(images)
	}

	fun getImages(noteId: Int): Flow<List<DatabaseImageEntity>> {
		return imageDao.getImages(noteId)
	}

	suspend fun updateImage(image: DatabaseImageEntity) {
		imageDao.updateImage(image)
	}

	suspend fun deleteImages(images: List<DatabaseImageEntity>) {
		imageDao.deleteImages(images)
	}
}