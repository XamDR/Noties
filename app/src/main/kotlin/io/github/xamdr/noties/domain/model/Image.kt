package io.github.xamdr.noties.domain.model

import android.net.Uri
import android.os.Parcelable
import io.github.xamdr.noties.data.entity.image.DatabaseImageEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(
	val id: Int = 0,
	val uri: Uri? = null,
	val mimeType: String? = null,
	val description: String? = null,
	val noteId: Long = 0
) : Parcelable {

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
