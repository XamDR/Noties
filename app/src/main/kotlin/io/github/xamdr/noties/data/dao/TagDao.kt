package io.github.xamdr.noties.data.dao

import androidx.room.*
import io.github.xamdr.noties.data.entity.tag.DatabaseTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

	@Insert
	suspend fun insertTag(tagEntity: DatabaseTagEntity)

	@Query("SELECT * FROM Tags ORDER BY name")
	fun getTags(): Flow<List<DatabaseTagEntity>>

	@Query("SELECT name FROM Tags")
	suspend fun getTagNames(): List<String>

	@Update
	suspend fun updateTag(tagEntity: DatabaseTagEntity)

	@Delete
	suspend fun deleteTags(tagEntities: List<DatabaseTagEntity>)
}