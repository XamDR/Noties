package io.github.xamdr.noties.data.repository

import io.github.xamdr.noties.data.dao.UrlDao
import io.github.xamdr.noties.data.entity.url.DatabaseUrlEntity
import io.github.xamdr.noties.domain.model.UrlItem
import io.github.xamdr.noties.domain.model.getMetadata
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UrlRepository @Inject constructor(private val urlDao: UrlDao) {

	suspend fun saveUrl(noteId: Long, url: String) {
		val metadata = url.getMetadata()
		val urlItem = UrlItem(
			source = url,
			metadata = metadata,
			noteId = noteId
		)
		urlDao.insertUrl(urlItem.asDatabaseEntity())
	}

	fun getUrls(sources: List<String>): Flow<List<DatabaseUrlEntity>> = urlDao.getUrls(sources)

	suspend fun getUrlsAsync(sources: List<String>): List<DatabaseUrlEntity> = urlDao.getUrlsAsync(sources)

	suspend fun deleteUrl(urlEntity: DatabaseUrlEntity) = urlDao.deleteUrl(urlEntity)
}