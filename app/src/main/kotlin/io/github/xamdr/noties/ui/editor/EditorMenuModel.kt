package io.github.xamdr.noties.ui.editor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.NotificationAdd
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.xamdr.noties.R

data class EditorMenuItem(val icon: ImageVector, val description: Int)

val EDITOR_MENU_ITEMS = listOf(
	EditorMenuItem(icon = Icons.Outlined.Attachment, description = R.string.add_attachment),
	EditorMenuItem(icon = Icons.Outlined.CameraAlt, description = R.string.take_picture),
	EditorMenuItem(icon = Icons.Outlined.Videocam, description = R.string.take_video),
	EditorMenuItem(icon = Icons.Outlined.CheckBox, description = R.string.add_todo_list),
	EditorMenuItem(icon = Icons.Outlined.NotificationAdd, description = R.string.add_reminder)
)