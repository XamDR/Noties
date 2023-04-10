package io.github.xamdr.noties.domain.usecase

import io.github.xamdr.noties.data.repository.TagRepository
import io.github.xamdr.noties.domain.model.Tag
import javax.inject.Inject

class CreateTagUseCase @Inject constructor(private val tagRepository: TagRepository) {

	suspend operator fun invoke(tag: Tag) {
		tagRepository.createTag(tag.asDatabaseEntity())
	}
}