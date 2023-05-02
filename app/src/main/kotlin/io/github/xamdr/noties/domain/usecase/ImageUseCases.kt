package io.github.xamdr.noties.domain.usecase

import io.github.xamdr.noties.data.repository.ImageRepository
import io.github.xamdr.noties.domain.model.Image
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InsertImagesUseCase @Inject constructor(private val imageRepository: ImageRepository) {

	suspend operator fun invoke(images: List<Image>) {
		imageRepository.insertImages(images.map { it.asDatabaseEntity() })
	}
}

class GetImagesUseCase @Inject constructor(private val imageRepository: ImageRepository) {

	operator fun invoke(noteId: Int): Flow<List<Image>> {
		return imageRepository.getImages(noteId).map { list ->
			list.map { it.asDomainModel() }
		}
	}
}

class UpdateImageUseCase @Inject constructor(private val imageRepository: ImageRepository) {

	suspend operator fun invoke(image: Image, description: String) {
		val updatedImage = image.copy(description = description)
		imageRepository.updateImage(updatedImage.asDatabaseEntity())
	}
}

class DeleteImagesUseCase @Inject constructor(private val imageRepository: ImageRepository) {

	suspend operator fun invoke(images: List<Image>) {
		imageRepository.deleteImages(images.map { it.asDatabaseEntity() })
	}
}