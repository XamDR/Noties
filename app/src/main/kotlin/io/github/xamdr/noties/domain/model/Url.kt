package io.github.xamdr.noties.domain.model

import android.os.Parcelable
import io.github.xamdr.noties.data.entity.url.DatabaseUrlEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class Url(
	val id: Int = 0,
	val source: String = String.Empty,
	val host: String? = null,
	val title: String? = null,
	val image: String? = null
) : Parcelable {

	fun asDatabaseEntity(): DatabaseUrlEntity {
		return DatabaseUrlEntity(
			id = id,
			source = source,
			host = host,
			title = title,
			image = image
		)
	}
}
