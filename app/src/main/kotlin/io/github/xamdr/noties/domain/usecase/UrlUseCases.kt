package io.github.xamdr.noties.domain.usecase

import io.github.xamdr.noties.data.repository.UrlRepository
import io.github.xamdr.noties.domain.model.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SaveUrlsUseCase @Inject constructor(private val urlRepository: UrlRepository) {

	suspend operator fun invoke(urls: List<String>) {
		urls.forEach { url -> urlRepository.saveUrl(url) }
	}
}

class GetUrlsUseCase @Inject constructor(private val urlRepository: UrlRepository) {

	operator fun invoke(sources: List<String>): Flow<List<Url>> {
		return urlRepository.getUrls(sources).map { list ->
			list.map { it.asDomainModel() }
		}
	}
}