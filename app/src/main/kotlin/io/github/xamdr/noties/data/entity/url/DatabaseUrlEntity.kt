package io.github.xamdr.noties.data.entity.url

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.xamdr.noties.domain.model.Url

@Entity(tableName = "Urls", indices = [Index(value = ["source"], unique = true)])
data class DatabaseUrlEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val source: String = String.Empty,
	val host: String? = null,
	val title: String? = null,
	val image: String? = null
) {

	fun asDomainModel(): Url {
		return Url(
			id = id,
			source = source,
			host = host,
			title = title,
			image = image
		)
	}
}
