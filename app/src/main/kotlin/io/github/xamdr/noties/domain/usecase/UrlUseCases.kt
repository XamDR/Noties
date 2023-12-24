package io.github.xamdr.noties.domain.usecase

import io.github.xamdr.noties.data.repository.UrlRepository
import io.github.xamdr.noties.domain.model.UrlItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SaveUrlsUseCase @Inject constructor(private val urlRepository: UrlRepository) {

	suspend operator fun invoke(noteId: Long, urls: List<String>) {
		urls.forEach { url -> urlRepository.saveUrl(noteId, url) }
	}
}

class GetUrlsUseCase @Inject constructor(private val urlRepository: UrlRepository) {

	operator fun invoke(sources: List<String>): Flow<List<UrlItem>> {
		return urlRepository.getUrls(sources).map { list ->
			list.map { it.asDomainModel() }
		}
	}
}

class GetUrlsAsyncUseCase @Inject constructor(private val urlRepository: UrlRepository) {

	suspend operator fun invoke(sources: List<String>): List<UrlItem> {
		return urlRepository.getUrlsAsync(sources).map { it.asDomainModel() }
	}
}