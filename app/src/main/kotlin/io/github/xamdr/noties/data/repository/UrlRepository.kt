package io.github.xamdr.noties.data.repository

import io.github.xamdr.noties.data.dao.UrlDao
import io.github.xamdr.noties.data.entity.url.DatabaseUrlEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UrlRepository @Inject constructor(private val urlDao: UrlDao) {

	suspend fun saveUrl(src: String) {
		val urlEntity = JsoupHelper.getMetadata(src)
		urlDao.insertUrl(urlEntity)
	}

	fun getUrls(sources: List<String>): Flow<List<DatabaseUrlEntity>> = urlDao.getUrls(sources)
}