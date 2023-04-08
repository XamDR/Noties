package io.github.xamdr.noties.core

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class Notebook(
	@Embedded val entity: NotebookEntity = NotebookEntity(),
	@Relation(
		parentColumn = "id",
		entityColumn = "notebook_id"
	)
	val notes: List<NoteEntity> = listOf()) : Parcelable