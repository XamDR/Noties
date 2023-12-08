package io.github.xamdr.noties.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
sealed class Task : Parcelable {
	data class Item(
		val id: String = UUID.randomUUID().toString(),
		val content: String = String.Empty,
		val done: Boolean = false
	) : Task()

	data object Footer : Task()
}

fun List<Task>.convertToString(): String {
	val items = this.filterIsInstance<Task.Item>()
	return items.joinToString(Note.NEWLINE) {
		if (it.done) {
			if (it.content.startsWith(Note.PREFIX_DONE)) it.content
			else "${Note.PREFIX_DONE}${it.content}"
		}
		else {
			if (it.content.startsWith(Note.PREFIX_NOT_DONE)) it.content
			else "${Note.PREFIX_NOT_DONE}${it.content}"
		}
	}
}

fun List<Task>.joinToString(): String {
	val items = this.filterIsInstance<Task.Item>()
	return items.joinToString(Note.NEWLINE) { it.content }
}

fun List<Task>.containsItem(item: Task.Item): Boolean {
	val contents = this.filterIsInstance<Task.Item>().map { it.content }
	return contents.contains(item.content)
}