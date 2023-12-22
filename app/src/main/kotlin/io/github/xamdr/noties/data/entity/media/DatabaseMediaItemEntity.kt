package io.github.xamdr.noties.data.entity.media

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.domain.model.MediaItemMetadata

@Entity(
	tableName = "MediaItems",
	foreignKeys = [ForeignKey(
		entity = DatabaseNoteEntity::class,
		parentColumns = ["id"],
		childColumns = ["note_id"],
		onDelete = ForeignKey.CASCADE
	)],
	indices = [Index(value = ["note_id"], unique = true)]
)
data class DatabaseMediaItemEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	@ColumnInfo(name = "uri") val uri: Uri = Uri.EMPTY,
	@ColumnInfo(name = "thumbnail_uri") val thumbnailUri: Uri? = null,
	@ColumnInfo(name = "mime_type") val mimeType: String? = null,
	@ColumnInfo(name = "media_type") val mediaType: MediaType = MediaType.Image,
	@ColumnInfo(name = "description") val description: String? = null,
	@ColumnInfo(name = "duration") val duration: Long = 0,
	@ColumnInfo(name = "note_id") val noteId: Long = 0
) {

	fun asDomainModel(): MediaItem {
		return MediaItem(
			id = this.id,
			uri = this.uri,
			mimeType = this.mimeType,
			mediaType = this.mediaType,
			metadata = MediaItemMetadata(this.thumbnailUri, this.duration),
			description = this.description,
			noteId = this.noteId
		)
	}
}
