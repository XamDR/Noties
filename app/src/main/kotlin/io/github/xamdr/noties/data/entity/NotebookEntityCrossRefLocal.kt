package io.github.xamdr.noties.data.entity

import androidx.room.Embedded
import androidx.room.Relation
import io.github.xamdr.noties.core.NoteEntity

data class NotebookEntityCrossRefLocal(
	@Embedded val entity: NotebookEntityLocal = NotebookEntityLocal(),
	@Relation(
		parentColumn = "id",
		entityColumn = "notebook_id"
	)
	val notes: List<NoteEntity> = listOf())