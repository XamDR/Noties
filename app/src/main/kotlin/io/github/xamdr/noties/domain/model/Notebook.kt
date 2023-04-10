package io.github.xamdr.noties.domain.model

import android.os.Parcelable
import io.github.xamdr.noties.core.NoteEntity
import io.github.xamdr.noties.data.entity.NotebookEntityLocal
import kotlinx.parcelize.Parcelize

@Parcelize
data class Notebook(
	val id: Int = 0,
	val name: String = String.Empty,
	val notes: List<NoteEntity> = listOf()
) : Parcelable {

	val noteCount = notes.size

	fun toEntityLocal(): NotebookEntityLocal {
		return NotebookEntityLocal(
			id = this.id,
			name = this.name,
			noteCount = this.noteCount
		)
	}
}
