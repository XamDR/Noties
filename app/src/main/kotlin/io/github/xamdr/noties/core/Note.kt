package io.github.xamdr.noties.core

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
	@Embedded var entity: NoteEntity = NoteEntity(),
	@Relation(
		parentColumn = "id",
		entityColumn = "note_id"
	) var images: List<ImageEntity> = emptyList()) : Parcelable {

	// We have to make a custom clone method because we can't override copy()
	// See: https://stackoverflow.com/q/47359496/8781554 to understand how
	// to make a deep copy of a Kotlin data class.
	fun clone(
		entity: NoteEntity = this.entity.copy(),
		images: List<ImageEntity> = this.images
	) = Note(entity, images)

	fun getPreviewImage() = images.firstOrNull()?.uri

	fun isNonEmpty() = entity.text.isNotEmpty() || images.isNotEmpty()

	fun toTodoList(): List<Todo.TodoItem> {
		return if (entity.text.isEmpty()) listOf(Todo.TodoItem())
		else entity.text.split(NEWLINE).map {
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
