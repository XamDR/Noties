package io.github.xamdr.noties.domain.usecase

import io.github.xamdr.noties.data.repository.TagRepository
import io.github.xamdr.noties.domain.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CreateTagUseCase @Inject constructor(private val tagRepository: TagRepository) {
	suspend operator fun invoke(tag: Tag) = tagRepository.createTag(tag.asDatabaseEntity())
}

class GetTagsUseCase @Inject constructor(private val tagRepository: TagRepository) {
	operator fun invoke(): Flow<List<Tag>> {
		return tagRepository.getTags().map { list ->
			list.map { it.asDomainModel() }
		}
	}
}

class GetTagNamesUseCase @Inject constructor(private val tagRepository: TagRepository) {
	suspend operator fun invoke(): List<String> = tagRepository.getTagNames()
}

class UpdateTagUseCase @Inject constructor(private val tagRepository: TagRepository) {
	suspend operator fun invoke(tag: Tag) = tagRepository.updateTag(tag.asDatabaseEntity())
}

class DeleteTagsUseCase @Inject constructor(private val tagRepository: TagRepository) {
	suspend operator fun invoke(tags: List<Tag>) {
		tagRepository.deleteTags(tags.map { it.asDatabaseEntity() })
	}
}