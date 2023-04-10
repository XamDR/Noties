package io.github.xamdr.noties.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Notebooks", indices = [Index(value = ["name"], unique = true)])
data class NotebookEntityLocal(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val name: String = String.Empty,
	@ColumnInfo(name = "note_count") val noteCount: Int = 0
)
