package io.github.xamdr.noties.ui.editor

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Task
import io.github.xamdr.noties.ui.components.TextBox
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.DragDropState
import io.github.xamdr.noties.ui.helpers.DraggableItem
import io.github.xamdr.noties.ui.helpers.dragContainerForHandle
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun TaskItem(
	task: Task,
	dragDropState: DragDropState,
	index: Int,
	onContentChanged: (String) -> Unit,
	onItemDone: (Boolean) -> Unit,
	onAddTask: () -> Unit,
	onRemoveTask: (Task) -> Unit,
) {
	when (task) {
		is Task.Item -> {
			DraggableItem(dragDropState = dragDropState, index = index) { isDragging ->
				val alpha by animateFloatAsState(if (isDragging) 0.5f else 1f, label = "")
				Row(
					modifier = Modifier
						.alpha(alpha)
						.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically
				) {
					IconButton(
						onClick = {},
						modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
					) {
						Icon(
							imageVector = Icons.Outlined.DragIndicator,
							contentDescription = stringResource(id = R.string.drag_item),
							modifier = Modifier.dragContainerForHandle(dragDropState = dragDropState, key = task.id)
						)
					}
					Checkbox(
						checked = task.done,
						onCheckedChange = onItemDone,
						modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
					)
					TextBox(
						placeholder = stringResource(id = R.string.placeholder),
						value = task.content,
						onValueChange = onContentChanged,
						modifier = Modifier.padding(vertical = 4.dp),
						textDecoration = if (task.done) TextDecoration.LineThrough else null
					)
					IconButton(
						onClick = { onRemoveTask(task) },
						modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
					) {
						Icon(
							imageVector = Icons.Outlined.Close,
							contentDescription = stringResource(id = R.string.remove_item)
						)
					}
				}
			}
		}
		Task.Footer -> {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(
					onClick = onAddTask,
					modifier = Modifier.padding(start = 48.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
				) {
					Icon(
						imageVector = Icons.Outlined.Add,
						contentDescription = null
					)
				}
				Text(
					text = stringResource(id = R.string.new_todo_item),
					modifier = Modifier.padding(top = 8.dp, end = 16.dp, bottom = 8.dp)
				)
			}
		}
	}
}

@Composable
private fun TaskItem(task: Task) {
	when (task) {
		is Task.Item -> {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(
					onClick = {},
					modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
				) {
					Icon(
						imageVector = Icons.Outlined.DragIndicator,
						contentDescription = stringResource(id = R.string.drag_item)
					)
				}
				Checkbox(
					checked = false,
					onCheckedChange = {},
					modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
				)
				TextBox(
					placeholder = stringResource(id = R.string.placeholder),
					value = "",
					onValueChange = {},
					modifier = Modifier.padding(vertical = 4.dp)
				)
				IconButton(
					onClick = {},
					modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
				) {
					Icon(
						imageVector = Icons.Outlined.Close,
						contentDescription = stringResource(id = R.string.remove_item)
					)
				}
			}
		}
		Task.Footer -> {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(
					onClick = {},
					modifier = Modifier.padding(start = 48.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
				) {
					Icon(
						imageVector = Icons.Outlined.Add,
						contentDescription = null
					)
				}
				Text(
					text = stringResource(id = R.string.new_todo_item),
					modifier = Modifier.padding(top = 8.dp, end = 16.dp, bottom = 8.dp)
				)
			}
		}
	}
}

@DevicePreviews
@Composable
private fun TaskItemPreview() = NotiesTheme { TaskItem(task = Task.Item(content = "")) }

@DevicePreviews
@Composable
private fun TaskItemFooterPreview() = NotiesTheme { TaskItem(task = Task.Footer) }