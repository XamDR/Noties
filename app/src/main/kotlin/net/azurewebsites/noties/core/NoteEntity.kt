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
	val title: String = String.Empty,
	val text: String = String.Empty,
	@ColumnInfo(name = "modification_date") val modificationDate: ZonedDateTime = ZonedDateTime.now(),
	@ColumnInfo(name = "color") val color: Int? = null,
	@ColumnInfo(name = "urls") val urls: List<String> = emptyList(),
	@ColumnInfo(name = "is_protected") val isProtected: Boolean = false,
	@ColumnInfo(name = "is_trashed") val isTrashed: Boolean = false,
	@ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
	@ColumnInfo(name = "is_todo_list") val isTodoList: Boolean = false,
	@ColumnInfo(name = "reminder_date") val reminderDate: ZonedDateTime = ZonedDateTime.now(),
	@ColumnInfo(name = "notebook_id") val notebookId: Int = 0) : Parcelable {

	fun getUrlCount() = urls.size
}
