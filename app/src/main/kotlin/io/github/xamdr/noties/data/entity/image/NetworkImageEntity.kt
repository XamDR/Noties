package io.github.xamdr.noties.data.entity.image

import android.net.Uri

data class NetworkImageEntity(
	val id: Int = 0,
	val uri: Uri? = null,
	val mimeType: String? = null,
	val description: String? = null,
	val noteId: Long = 0
) {

	fun asDatabaseEntity(): DatabaseImageEntity {
		return DatabaseImageEntity(
			id = this.id,
			uri = this.uri,
			mimeType = this.mimeType,
			description = this.description,
			noteId = this.noteId
		)
	}
}
