package io.github.xamdr.noties.domain.model

import android.os.Parcelable
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class Note(
	val id: Long = 0L,
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
	val tags: List<String> = emptyList(),
	var images: List<Image> = emptyList()
) : Parcelable {

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

	fun getPreviewImage() = images.firstOrNull()?.uri

	fun isEmpty() = text.isEmpty() && images.isEmpty()

	fun toTodoList(): List<Todo.TodoItem> {
		return if (text.isEmpty()) listOf(Todo.TodoItem())
		else text.split(NEWLINE).map {
			if (it.startsWith(PREFIX_DONE)) {
				Todo.TodoItem(content = it.removePrefix(PREFIX_DONE), done = true)
			}
			else Todo.TodoItem(content = it.removePrefix(PREFIX_NOT_DONE))
		}
	}

	companion object {
		const val NEWLINE = "\n"
		// These prefixes are based on the extended Markdown syntax for task lists.
		const val PREFIX_NOT_DONE = "- [ ] "
		const val PREFIX_DONE = "- [x] "
	}
}
