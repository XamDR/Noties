package io.github.xamdr.noties.ui.editor

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.NotificationAdd
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem

data class EditorMenuItem(
	val id: Int,
	val icon: ImageVector,
	val description: Int
)

val EDITOR_MENU_ITEMS = listOf(
	EditorMenuItem(id = R.id.attach_media, icon = Icons.Outlined.Attachment, description = R.string.add_attachment),
	EditorMenuItem(id = R.id.take_picture, icon = Icons.Outlined.CameraAlt, description = R.string.take_picture),
	EditorMenuItem(id = R.id.record_video, icon = Icons.Outlined.Videocam, description = R.string.take_video),
	EditorMenuItem(id = R.id.add_task_list, icon = Icons.Outlined.CheckBox, description = R.string.add_todo_list),
	EditorMenuItem(id = R.id.add_reminder, icon = Icons.Outlined.NotificationAdd, description = R.string.add_reminder)
)

sealed class GridItem(open val src: Uri) {
	data class Media(val data: MediaItem) : GridItem(data.uri)
	data class AndroidUri(override val src: Uri) : GridItem(src)
}