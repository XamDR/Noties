package net.azurewebsites.noties.core

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Parcelize
@Entity(tableName = "Notes")
data class NoteEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val title: String? = null,
	val text: String = String.Empty,
	@ColumnInfo(name = "date_modification") val dateModification: ZonedDateTime = ZonedDateTime.now(),
	@ColumnInfo(name = "color") val color: Int = 0,
	@ColumnInfo(name = "urls") val urls: List<String> = emptyList(),
	@ColumnInfo(name = "is_pinned") val pinned: Boolean = false,
	@ColumnInfo(name = "is_trashed") val isTrashed: Boolean = false,
	@ColumnInfo(name = "folder_id") val folderId: Int = 0) : Parcelable {

	fun getUrlCount() = urls.size
}
