package io.github.xamdr.noties.core

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Notebooks", indices = [Index(value = ["name"], unique = true)])
data class NotebookEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val name: String = String.Empty,
	@ColumnInfo(name = "note_count") val noteCount: Int = 0) : Parcelable
