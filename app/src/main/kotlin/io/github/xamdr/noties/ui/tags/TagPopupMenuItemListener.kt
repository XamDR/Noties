package io.github.xamdr.noties.ui.tags

import io.github.xamdr.noties.domain.model.Tag

interface TagPopupMenuItemListener {
	fun showCreateTagDialog(tag: Tag)
	fun deleteTag(tag: Tag)
}