package io.github.xamdr.noties.ui.editor

import android.net.Uri
import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.NotificationAdd
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem
import kotlinx.parcelize.Parcelize

data class EditorMenuItem(
	val id: Int,
	val icon: ImageVector,
	val description: Int
)

val EDITOR_MENU_ITEMS = listOf(
	EditorMenuItem(id = R.id.gallery, icon = Icons.Outlined.Image, description = R.string.gallery),
	EditorMenuItem(id = R.id.camera, icon = Icons.Outlined.CameraAlt, description = R.string.camera),
//	EditorMenuItem(id = R.id.record_video, icon = Icons.Outlined.Videocam, description = R.string.take_video),
	EditorMenuItem(id = R.id.todo_list, icon = Icons.Outlined.CheckBox, description = R.string.todo_list),
	EditorMenuItem(id = R.id.reminder, icon = Icons.Outlined.NotificationAdd, description = R.string.reminder),
	EditorMenuItem(id = R.id.tags, icon = Icons.Outlined.NewLabel, description = R.string.tags)
)

@Parcelize
sealed class GridItem(open val src: Uri) : Parcelable {
	data class Media(val data: MediaItem) : GridItem(data.uri)
	data class AndroidUri(override val src: Uri) : GridItem(src)
}