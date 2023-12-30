package io.github.xamdr.noties.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import io.github.xamdr.noties.data.entity.media.DatabaseMediaItemEntity

@Dao
interface MediaDao {

	@Insert
	suspend fun insertMediaItems(mediaItemList: List<DatabaseMediaItemEntity>)

	@Update
	suspend fun updateMediaItems(mediaItemList: List<DatabaseMediaItemEntity>)

	@Delete
	suspend fun deleteMediaItems(mediaItemList: List<DatabaseMediaItemEntity>)
}