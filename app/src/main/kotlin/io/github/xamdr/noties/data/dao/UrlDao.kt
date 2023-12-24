package io.github.xamdr.noties.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.xamdr.noties.data.entity.url.DatabaseUrlEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UrlDao {

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertUrl(urlEntity: DatabaseUrlEntity)

	@Query("SELECT * FROM Urls WHERE source IN (:sources) ORDER BY id")
	fun getUrls(sources: List<String>): Flow<List<DatabaseUrlEntity>>

	@Query("SELECT * FROM Urls WHERE source IN (:sources) ORDER BY id")
	suspend fun getUrlsAsync(sources: List<String>): List<DatabaseUrlEntity>
}