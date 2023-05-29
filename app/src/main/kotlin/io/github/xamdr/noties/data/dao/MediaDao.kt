package io.github.xamdr.noties.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import io.github.xamdr.noties.data.entity.media.DatabaseMediaItemEntity

@Dao
interface MediaDao {

	@Insert
	suspend fun insertMediaItems(images: List<DatabaseMediaItemEntity>)

	@Delete
	suspend fun deleteMediaItems(videos: List<DatabaseMediaItemEntity>)
}