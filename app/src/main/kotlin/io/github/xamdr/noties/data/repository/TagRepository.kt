package io.github.xamdr.noties.data.repository

import io.github.xamdr.noties.data.dao.TagDao
import io.github.xamdr.noties.data.entity.tag.DatabaseTagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TagRepository @Inject constructor(private val tagDao: TagDao) {

	suspend fun createTag(tagEntity: DatabaseTagEntity) = tagDao.insertTag(tagEntity)

	fun getTags(): Flow<List<DatabaseTagEntity>> = tagDao.getTags()

	suspend fun getTagNames(): List<String> = tagDao.getTagNames()

	suspend fun updateTag(tagEntity: DatabaseTagEntity) = tagDao.updateTag(tagEntity)

	suspend fun deleteTag(tagEntity: DatabaseTagEntity) = tagDao.deleteTag(tagEntity)
}