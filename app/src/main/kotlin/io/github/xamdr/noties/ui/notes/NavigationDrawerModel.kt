package io.github.xamdr.noties.ui.notes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.xamdr.noties.R

sealed class DrawerItem(open val id: Int) {
	data class Header(override val id: Int, val label: Int) : DrawerItem(id)
	data class TagItem(override val id: Int, val icon: ImageVector, val label: String) : DrawerItem(id)
	data class DefaultItem(override val id: Int, val icon: ImageVector, val label: Int) : DrawerItem(id)
}

val DEFAULT_DRAWER_ITEMS = listOf(
	DrawerItem.DefaultItem(id = R.string.all_notes, icon = Icons.Outlined.Article, label = R.string.all_notes),
	DrawerItem.DefaultItem(id = R.string.reminders, icon = Icons.Outlined.Notifications, label = R.string.reminders),
	DrawerItem.DefaultItem(id = R.string.locked_notes, icon = Icons.Outlined.Lock, label = R.string.locked_notes),
	DrawerItem.Header(id = R.string.tags, label = R.string.tags),
	DrawerItem.DefaultItem(id = R.string.create_tag, icon = Icons.Outlined.NewLabel, label = R.string.create_tag),
	DrawerItem.DefaultItem(id = R.string.archived_notes, icon = Icons.Outlined.Archive, label = R.string.archived_notes),
	DrawerItem.DefaultItem(id = R.string.recycle_bin, icon = Icons.Outlined.Delete, label = R.string.recycle_bin),
	DrawerItem.DefaultItem(id = R.string.settings, icon = Icons.Outlined.Settings, label = R.string.settings)
)