package io.github.xamdr.noties.domain.model

import android.net.Uri
import android.os.Parcelable
import io.github.xamdr.noties.data.entity.media.DatabaseMediaItemEntity
import io.github.xamdr.noties.data.entity.media.MediaType
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaItem(
	val id: Int = 0,
	val uri: Uri = Uri.EMPTY,
	val mimeType: String? = null,
	val mediaType: MediaType = MediaType.Image,
	val metadata: MediaItemMetadata = MediaItemMetadata(),
	val description: String? = null,
	val noteId: Long = 0
) : Parcelable {

	fun asDatabaseEntity(): DatabaseMediaItemEntity {
		return DatabaseMediaItemEntity(
			id = this.id,
			uri = this.uri,
			thumbnailUri = this.metadata.thumbnail,
			mimeType = this.mimeType,
			mediaType = this.mediaType,
			description = this.description,
			duration = this.metadata.duration,
			noteId = this.noteId
		)
	}
}