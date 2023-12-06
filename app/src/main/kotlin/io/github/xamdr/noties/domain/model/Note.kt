package io.github.xamdr.noties.domain.model

import android.os.Parcelable
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
	val id: Long = 0L,
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
	val tags: List<String> = emptyList(),
	val items: List<MediaItem> = emptyList()
) : Parcelable {

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

	val previewItem: MediaItem?
		get() = items.firstOrNull()

	fun isEmpty() = text.isEmpty() && items.isEmpty()

	fun toTaskList(): List<Task.Item> {
		return if (text.isEmpty()) listOf(Task.Item())
		else text.split(NEWLINE).map {
			if (it.startsWith(PREFIX_DONE)) {
				Task.Item(content = it.removePrefix(PREFIX_DONE), done = true)
			}
			else Task.Item(content = it.removePrefix(PREFIX_NOT_DONE))
		}
	}

	companion object {
		const val NEWLINE = "\n"
		// These prefixes are based on the extended Markdown syntax for task lists.
		const val PREFIX_NOT_DONE = "- [ ] "
		const val PREFIX_DONE = "- [x] "

		fun create(note: Note, items: List<MediaItem>): Note {
			return Note(
				id = note.id,
				title = note.title,
				text = note.text,
				modificationDate = note.modificationDate,
				color = note.color,
				urls = note.urls,
				protected = note.protected,
				trashed = note.trashed,
				pinned = note.pinned,
				isTaskList = note.isTaskList,
				reminderDate = note.reminderDate,
				tags = note.tags,
				items = items
			)
		}
	}
}
