package net.azurewebsites.noties.core

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class Folder(
	@Embedded val entity: FolderEntity = FolderEntity(),
	@Relation(
		parentColumn = "id",
		entityColumn = "folder_id"
	)
	val notes: List<NoteEntity> = listOf()) : Parcelable