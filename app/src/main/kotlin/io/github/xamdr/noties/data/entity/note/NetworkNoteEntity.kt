package io.github.xamdr.noties.data.entity.note

data class NetworkNoteEntity(
	val id: Long = 0,
	val title: String = String.Empty,
	val text: String = String.Empty,
	val modificationDate: Long = 0,
	val color: Int? = null,
	val urls: List<String> = emptyList(),
	val protected: Boolean = false,
	val trashed: Boolean = false,
	val archived: Boolean = false,
	val pinned: Boolean = false,
	val isTaskList: Boolean = false,
	val reminderDate: Long? = null,
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
			protected = this.protected,
			trashed = this.trashed,
			archived = this.archived,
			pinned = this.pinned,
			isTaskList = this.isTaskList,
			reminderDate = this.reminderDate,
			tags = this.tags
		)
	}
}
