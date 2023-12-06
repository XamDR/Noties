package io.github.xamdr.noties.data.entity.note

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.xamdr.noties.domain.model.Note

@Entity(tableName = "Notes")
data class DatabaseNoteEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val title: String = String.Empty,
	val text: String = String.Empty,
	@ColumnInfo(name = "modification_date") val modificationDate: Long = 0,
	@ColumnInfo(name = "color") val color: Int? = null,
	@ColumnInfo(name = "urls") val urls: List<String> = emptyList(),
	@ColumnInfo(name = "protected") val protected: Boolean = false,
	@ColumnInfo(name = "trashed") val trashed: Boolean = false,
	@ColumnInfo(name = "archived") val archived: Boolean = false,
	@ColumnInfo(name = "pinned") val pinned: Boolean = false,
	@ColumnInfo(name = "has_task_list") val isTaskList: Boolean = false,
	@ColumnInfo(name = "reminder_date") val reminderDate: Long? = null,
	@ColumnInfo(name = "tags") val tags: List<String> = emptyList()
) {

	fun asDomainModel(): Note {
		return Note(
			id = this.id,
			title = this.title,
			text = this.text,
			modificationDate = this.modificationDate,
			color = this.color,
			urls = this.urls,
			protected = this.protected,
			trashed = this.trashed,
			pinned = this.pinned,
			isTaskList = this.isTaskList,
			reminderDate = this.reminderDate,
			tags = this.tags
		)
	}
}
