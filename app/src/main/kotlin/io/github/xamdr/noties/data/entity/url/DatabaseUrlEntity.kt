package io.github.xamdr.noties.data.entity.url

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity
import io.github.xamdr.noties.domain.model.UrlItem
import io.github.xamdr.noties.domain.model.UrlMetadata

@Entity(
	tableName = "Urls",
	foreignKeys = [ForeignKey(
		entity = DatabaseNoteEntity::class,
		parentColumns = ["id"],
		childColumns = ["note_id"],
		onDelete = ForeignKey.CASCADE
	)],
	indices = [Index(value = ["source"], unique = true), Index(value = ["note_id"])]
)
data class DatabaseUrlEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val source: String = String.Empty,
	val host: String? = null,
	val title: String? = null,
	val image: String? = null,
	@ColumnInfo(name = "note_id") val noteId: Long = 0
) {

	fun asDomainModel(): UrlItem {
		return UrlItem(
			id = id,
			source = source,
			metadata = UrlMetadata(host = host, title = title, image = image)
		)
	}
}
