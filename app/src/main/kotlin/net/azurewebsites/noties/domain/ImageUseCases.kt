package net.azurewebsites.noties.domain

import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.data.ImageDao
import javax.inject.Inject

class InsertImageUseCase @Inject constructor(private val imageDao: ImageDao) {
	suspend operator fun invoke(images: List<ImageEntity>) = imageDao.insertImages(images)
}

class UpdateImageUseCase @Inject constructor(private val imageDao: ImageDao) {
	suspend operator fun invoke(image: ImageEntity, description: String) {
		val updatedImage = image.copy(description = description)
		imageDao.updateImage(updatedImage)
	}
}

class GetImagesUseCase @Inject constructor(private val imageDao: ImageDao) {
	operator fun invoke(noteId: Int) = imageDao.getImages(noteId)
}

class DeleteImagesUseCase @Inject constructor(private val imageDao: ImageDao) {
	suspend operator fun invoke(images: List<ImageEntity>) = imageDao.deleteImages(images)
}