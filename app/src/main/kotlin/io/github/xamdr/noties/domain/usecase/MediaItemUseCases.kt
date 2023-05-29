package io.github.xamdr.noties.domain.usecase

import io.github.xamdr.noties.data.repository.MediaItemRepository
import io.github.xamdr.noties.domain.model.MediaItem
import javax.inject.Inject

class DeleteMediaItemsUseCase @Inject constructor(private val mediaItemRepository: MediaItemRepository) {

	suspend operator fun invoke(items: List<MediaItem>) {
		val realItems = items.filter { item -> item.id != 0 }.map { it.asDatabaseEntity() }
		mediaItemRepository.deleteItems(realItems)
	}
}