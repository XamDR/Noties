package io.github.xamdr.noties.domain.model

import android.net.Uri
import io.github.xamdr.noties.data.entity.image.DatabaseImageEntity
import java.io.Serializable

data class Image(
	val id: Int = 0,
	val uri: Uri? = null,
	val mimeType: String? = null,
	val description: String? = null,
	val noteId: Long = 0
) : Serializable {

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
