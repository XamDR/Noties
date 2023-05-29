package io.github.xamdr.noties.data.entity.media

import android.net.Uri

data class NetworkMediaItemEntity(
	val id: Int = 0,
	val uri: Uri? = null,
	val thumbnailUri: Uri? = null,
	val mimeType: String? = null,
	val mediaType: MediaType = MediaType.Image,
	val description: String? = null,
	val duration: Long = 0,
	val noteId: Long = 0
) {

	fun asDatabaseEntity(): DatabaseMediaItemEntity {
		return DatabaseMediaItemEntity(
			id = this.id,
			uri = this.uri,
			thumbnailUri = this.thumbnailUri,
			mimeType = this.mimeType,
			mediaType = this.mediaType,
			description = this.description,
			duration = this.duration,
			noteId = this.noteId
		)
	}
}
