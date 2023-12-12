package io.github.xamdr.noties.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.model.Task
import io.github.xamdr.noties.ui.components.TextBox
import io.github.xamdr.noties.ui.helpers.DateTimeHelper
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.onFocusShowSoftKeyboard
import io.github.xamdr.noties.ui.helpers.rememberDragDropState
import io.github.xamdr.noties.ui.theme.NotiesTheme

private const val SPAN_COUNT = 2

@Suppress("USELESS_CAST")
@Composable
fun Editor(
	modifier: Modifier,
	note: Note,
	items: List<GridItem>,
	tasks: List<Task>,
	onNoteContentChange: (String) -> Unit,
	onItemCopied: (MediaItem, Int) -> Unit,
	onItemClick: (Int) -> Unit,
	onDateTagClick: () -> Unit,
	onTagClick: () -> Unit,
	onTaskContentChanged: (Int, String) -> Unit,
	onTaskDone: (Int, Boolean) -> Unit,
	onDragDropTask: (Int, Int) -> Unit,
	onAddTask: () -> Unit,
	onRemoveTask: (Task) -> Unit
) {
	val gridState = rememberLazyGridState()
	val dragDropState = rememberDragDropState(
		lazyGridState = gridState,
		onMove = onDragDropTask
	)
	val focusRequester = remember { FocusRequester() }
	val textBoxModifier = Modifier
		.fillMaxWidth()
		.then(if (note.id == 0L) Modifier.onFocusShowSoftKeyboard(focusRequester) else Modifier)

	LazyVerticalGrid(
		modifier = modifier,
		state = gridState,
		contentPadding = PaddingValues(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		columns = GridCells.Fixed(count = SPAN_COUNT)
	) {
		items(
			count = items.size,
			key = { index -> items[index].src },
			span = { index ->
				val span = if (items.size.mod(SPAN_COUNT) == 1 && index == 0) SPAN_COUNT else 1
				GridItemSpan(span)
			},
			itemContent = { index ->
				EditorGridItem(
					item = items[index],
					onUriCopied = { item -> onItemCopied(item, index) },
					onItemClick = { onItemClick(index) },
					isFullWidth = items.size.mod(SPAN_COUNT) == 1 && index == 0
				)
			}
		)
		if (note.isTaskList) {
			val allTasks = tasks + Task.Footer
			itemsIndexed(
				items = allTasks,
				key = { _, item -> if (item is Task.Item) item.id else String.Empty },
				span = { _, _ -> GridItemSpan(SPAN_COUNT) },
				itemContent = { index, task ->
					TaskItem(
						note = note,
						task = task,
						dragDropState = dragDropState,
						index = index,
						offset = items.size,
						onContentChanged = { onTaskContentChanged(index, it) },
						onItemDone = { onTaskDone(index, it) },
						onAddTask = onAddTask,
						onRemoveTask = onRemoveTask
					)
				}
			)
		}
		else {
			item(span = { GridItemSpan(SPAN_COUNT) }) {
				TextBox(
					placeholder = stringResource(id = R.string.placeholder),
					value = note.text,
					onValueChange = onNoteContentChange,
					readOnly = note.trashed,
					modifier = textBoxModifier
				)
			}
		}
		item(span = { GridItemSpan(SPAN_COUNT) }) {
			if (note.tags.isNotEmpty() || note.reminderDate != null) {
				val allTags = when {
					note.tags.isNotEmpty() && note.reminderDate != null -> listOf(DateTimeHelper.formatDateTime(note.reminderDate)) + note.tags
					note.reminderDate != null -> listOf(DateTimeHelper.formatDateTime(note.reminderDate))
					else -> note.tags
				}
				TagList(
					tags = allTags,
					onDateTagClick = if (note.trashed) ({} as () -> Unit) else onDateTagClick,
					onTagClick = if (note.trashed) ({} as () -> Unit) else onTagClick
				)
			}
		}
	}
}

@Composable
fun Editor(modifier: Modifier) {
	val items = listOf(1, 2, 3)
	LazyVerticalGrid(
		modifier = modifier,
		contentPadding = PaddingValues(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		columns = GridCells.Fixed(count = SPAN_COUNT)
	) {
		items(
			count = items.size,
			span = { index ->
				val span = if (items.size.mod(SPAN_COUNT) == 1 && index == 0) SPAN_COUNT else 1
				GridItemSpan(span)
			},
			itemContent = { EditorGridItem() }
		)
		item(span = { GridItemSpan(SPAN_COUNT) }) {
			TextBox(
				placeholder = stringResource(id = R.string.placeholder),
				value = stringResource(id = R.string.demo_content),
				onValueChange = {},
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}

@DevicePreviews
@Composable
private fun EditorPreview() = NotiesTheme { Editor(modifier = Modifier.fillMaxSize()) }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagList(
	tags: List<String>,
	onDateTagClick: () -> Unit,
	onTagClick: () -> Unit
) {
	FlowRow(
		modifier = Modifier
			.fillMaxWidth()
			.padding(all = 8.dp),
		horizontalArrangement = Arrangement.Start
	) {
		tags.forEach { tag ->
			if (DateTimeHelper.isValidDate(tag)) {
				AssistChip(
					onClick = onDateTagClick,
					label = {
						Text(
							text = tag,
							textDecoration = if (DateTimeHelper.isPast(tag)) TextDecoration.LineThrough else null
						)
					},
					leadingIcon = {
						Icon(
							imageVector = Icons.Outlined.Alarm,
							contentDescription = null,
							modifier = Modifier.size(AssistChipDefaults.IconSize)
						)
					},
					modifier = Modifier.padding(horizontal = 4.dp)
				)
			}
			else {
				SuggestionChip(
					onClick = onTagClick,
					label = { Text(text = tag) },
					modifier = Modifier.padding(horizontal = 4.dp)
				)
			}
		}
	}
}

@DevicePreviews
@Composable
private fun TagListPreview() {
	val tags = listOf("Android", "iOS", "Windows")
	NotiesTheme {
		TagList(
			tags = tags,
			onDateTagClick = {},
			onTagClick = {}
		)
	}
}