package io.github.xamdr.noties.data.entity.note

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.xamdr.noties.domain.model.Note
import java.time.LocalDateTime

@Entity(tableName = "Notes")
data class DatabaseNoteEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val title: String = String.Empty,
	val text: String = String.Empty,
	@ColumnInfo(name = "modification_date") val modificationDate: LocalDateTime = LocalDateTime.now(),
	@ColumnInfo(name = "color") val color: Int? = null,
	@ColumnInfo(name = "urls") val urls: List<String> = emptyList(),
	@ColumnInfo(name = "is_protected") val isProtected: Boolean = false,
	@ColumnInfo(name = "is_trashed") val isTrashed: Boolean = false,
	@ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
	@ColumnInfo(name = "is_todo_list") val isTaskList: Boolean = false,
	@ColumnInfo(name = "reminder_date") val reminderDate: LocalDateTime? = null,
	@ColumnInfo(name = "tags") val tags: List<String> = emptyList()) {

	fun asDomainModel(): Note {
		return Note(
			id = this.id,
			title = this.title,
			text = this.text,
			modificationDate = this.modificationDate,
			color = this.color,
			urls = this.urls,
			isProtected = this.isProtected,
			isTrashed = this.isTrashed,
			isPinned = this.isPinned,
			isTaskList = this.isTaskList,
			reminderDate = this.reminderDate,
			tags = this.tags
		)
	}
}
