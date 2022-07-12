package net.azurewebsites.noties.core

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
	@Embedded val entity: NoteEntity = NoteEntity(),
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

	fun toTodoList() = if (entity.text.isEmpty()) listOf(DataItem.TodoItem())
		else entity.text.split('\n').map { DataItem.TodoItem(content = it) }
}
