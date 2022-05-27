package net.azurewebsites.noties.core

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Folders", indices = [Index(value = ["name"], unique = true)])
data class FolderEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val name: String = String.Empty,
	@ColumnInfo(name = "note_count") val noteCount: Int = 0,
	@ColumnInfo(name = "is_protected") val isProtected: Boolean = false) : Parcelable
