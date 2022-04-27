package net.azurewebsites.noties.domain

import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import net.azurewebsites.noties.util.Empty
import java.time.ZonedDateTime

@Parcelize
@Entity(tableName = "Notes")
data class NoteEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	var title: String? = null,
	var text: String = String.Empty,
	@ColumnInfo(name = "update_date") val updateDate: ZonedDateTime = ZonedDateTime.now(),
	@ColumnInfo(name = "color") val color: Int = 0,
	@ColumnInfo(name = "urls") val urls: List<String> = emptyList(),
	@ColumnInfo(name = "preview_image") val previewImage: Uri? = null,
	@ColumnInfo(name = "is_pinned") val pinned: Boolean = false,
	@ColumnInfo(name = "is_task_list") val isTaskList: Boolean = false,
	@ColumnInfo(name = "directory_id") val directoryId: Int = 0) : Parcelable {

	fun getUrlCount() = urls.size

	fun toTaskList() = if (text.isEmpty()) emptyList()
		else text.split("\n").map { Task(content = it) }
}
