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
	var title: String = String.Empty,
	var text: String = String.Empty,
	@ColumnInfo(name = "date_modification") val dateModification: ZonedDateTime = ZonedDateTime.now(),
	@ColumnInfo(name = "color") val color: Int = 0,
	@ColumnInfo(name = "urls") val urls: List<String> = emptyList(),
	@ColumnInfo(name = "is_protected") val isProtected: Boolean = false,
	@ColumnInfo(name = "is_trashed") val isTrashed: Boolean = false,
	@ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
	@ColumnInfo(name = "is_todo_list") val isTodoList: Boolean = false,
	@ColumnInfo(name = "notebook_id") val notebookId: Int = 0) : Parcelable {

	fun getUrlCount() = urls.size

	fun toTodoList() = if (text.isEmpty()) listOf(Todo())
		else text.split('\n').map { Todo(content = it) }
}
