package io.github.xamdr.noties.data.repository

import io.github.xamdr.noties.data.dao.TagDao
import io.github.xamdr.noties.data.entity.tag.DatabaseTagEntity
import javax.inject.Inject

class TagRepository @Inject constructor(private val tagDao: TagDao) {

	suspend fun createTag(tagEntity: DatabaseTagEntity) = tagDao.insertTag(tagEntity)
}