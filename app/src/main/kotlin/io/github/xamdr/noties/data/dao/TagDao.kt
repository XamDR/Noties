package io.github.xamdr.noties.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import io.github.xamdr.noties.data.entity.tag.DatabaseTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

	@Insert
	suspend fun insertTag(tagEntity: DatabaseTagEntity)

	@Transaction
	@Query("SELECT * FROM Tags ORDER BY name")
	fun getTags(): Flow<List<DatabaseTagEntity>>
}