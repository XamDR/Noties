package io.github.xamdr.noties.data.entity.image

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.xamdr.noties.domain.model.Image

@Entity(tableName = "Images")
data class DatabaseImageEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val uri: Uri? = null,
	@ColumnInfo(name = "mime_type") val mimeType: String? = null,
	@ColumnInfo(name = "alt_text") val description: String? = null,
	@ColumnInfo(name = "note_id") val noteId: Long = 0
) {

	fun asDomainModel(): Image {
		return Image(
			id = this.id,
			uri = this.uri,
			mimeType = this.mimeType,
			description = this.description,
			noteId = this.noteId
		)
	}
}
