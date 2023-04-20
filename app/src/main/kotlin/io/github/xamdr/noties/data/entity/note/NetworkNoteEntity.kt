package io.github.xamdr.noties.data.entity.note

import java.time.LocalDateTime

data class NetworkNoteEntity(
	val id: Long = 0,
	val title: String = String.Empty,
	val text: String = String.Empty,
	val modificationDate: LocalDateTime = LocalDateTime.now(),
	val color: Int? = null,
	val urls: List<String> = emptyList(),
	val isProtected: Boolean = false,
	val isTrashed: Boolean = false,
	val isPinned: Boolean = false,
	val isTodoList: Boolean = false,
	val reminderDate: LocalDateTime? = null,
	val tags: List<String> = emptyList()
) {

	fun asDatabaseEntity(): DatabaseNoteEntity {
		return DatabaseNoteEntity(
			id = this.id,
			title = this.title,
			text = this.text,
			modificationDate = this.modificationDate,
			color = this.color,
			urls = this.urls,
			isProtected = this.isProtected,
			isTrashed = this.isTrashed,
			isPinned = this.isPinned,
			isTodoList = this.isTodoList,
			reminderDate = this.reminderDate,
			tags = this.tags
		)
	}
}
