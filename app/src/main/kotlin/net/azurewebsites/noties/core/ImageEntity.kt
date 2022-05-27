package net.azurewebsites.noties.core

import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Images")
data class ImageEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val uri: Uri? = null,
	@ColumnInfo(name = "mime_type") val mimeType: String? = null,
	@ColumnInfo(name = "alt_text") val description: String? = null,
	@ColumnInfo(name = "note_id") var noteId: Long = 0) : Parcelable
