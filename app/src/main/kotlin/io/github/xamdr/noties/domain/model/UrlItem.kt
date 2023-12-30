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
			id = this.id,
			source = this.source,
			host = this.metadata.host,
			title = this.metadata.title,
			image = this.metadata.image,
			noteId = this.noteId
		)
	}
}
