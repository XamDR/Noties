package io.github.xamdr.noties.domain.model

import android.os.Parcelable
import io.github.xamdr.noties.data.entity.url.DatabaseUrlEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class UrlItem(
	val id: Int = 0,
	val source: Url = String.Empty,
	val metadata: UrlMetadata = UrlMetadata(),
	val noteId: Long = 0
) : Parcelable {

	fun asDatabaseEntity(): DatabaseUrlEntity {
		return DatabaseUrlEntity(
			id = id,
			source = source,
			host = metadata.host,
			title = metadata.title,
			image = metadata.image,
			noteId = noteId
		)
	}
}
