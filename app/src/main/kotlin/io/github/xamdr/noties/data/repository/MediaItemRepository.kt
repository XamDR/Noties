package io.github.xamdr.noties.data.repository

import io.github.xamdr.noties.data.dao.MediaDao
import io.github.xamdr.noties.data.entity.media.DatabaseMediaItemEntity

class MediaItemRepository(private val mediaDao: MediaDao) {

	suspend fun insertItems(items: List<DatabaseMediaItemEntity>) {
		mediaDao.insertMediaItems(items)
	}

	suspend fun updateItems(items: List<DatabaseMediaItemEntity>) {
		mediaDao.updateMediaItems(items)
	}
	
	suspend fun deleteItems(items: List<DatabaseMediaItemEntity>) {
		mediaDao.deleteMediaItems(items)
	}
}