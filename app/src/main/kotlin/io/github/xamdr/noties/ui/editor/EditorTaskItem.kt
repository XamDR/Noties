package io.github.xamdr.noties.ui.editor

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.model.Task
import io.github.xamdr.noties.ui.components.TextBox
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.DragDropState
import io.github.xamdr.noties.ui.helpers.DraggableItem
import io.github.xamdr.noties.ui.helpers.dragContainerForHandle
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun TaskItem(
	note: Note,
	task: Task,
	dragDropState: DragDropState,
	index: Int,
	offset: Int,
	onContentChanged: (String) -> Unit,
	onItemDone: (Boolean) -> Unit,
	onAddTask: () -> Unit,
	onRemoveTask: (Task) -> Unit,
) {
	var showRemoveTaskButton by rememberSaveable { mutableStateOf(value = false) }
	val focusManager = LocalFocusManager.current

	fun addTask() {
		onAddTask()
		focusManager.moveFocus(FocusDirection.Down)
	}

	fun removeTask() {
		onRemoveTask(task)
		focusManager.moveFocus(FocusDirection.Up)
	}

	fun onEnterKeyPress(input: String) {
		if (input.contains("\n")) {
			input.replace("\n", String.Empty)
			addTask()
		}
		else {
			onContentChanged(input)
		}
	}

	fun onDeleteKeyPress(event: KeyEvent): Boolean {
		if (event.type == KeyEventType.KeyUp &&
			event.key == Key.Backspace &&
			(task as Task.Item).content.isEmpty()
		) {
			removeTask()
			return true
		}
		return false
	}

	when (task) {
		is Task.Item -> {
			DraggableItem(dragDropState = dragDropState, index = index, offset = offset) { isDragging ->
				val alpha by animateFloatAsState(if (isDragging) 0.5f else 1f, label = "alpha")
				val modifier = Modifier
					.padding(start = 8.dp, end = 2.dp)
					.then(if (note.trashed) Modifier else Modifier.dragContainerForHandle(dragDropState = dragDropState, key = task.id))

				Row(
					modifier = Modifier
						.alpha(alpha)
						.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = Icons.Outlined.DragIndicator,
						contentDescription = stringResource(id = R.string.drag_item),
						modifier = modifier
					)
					Checkbox(
						checked = task.done,
						onCheckedChange = if (note.trashed) null else onItemDone,
						modifier = Modifier
							.minimumInteractiveComponentSize()
							.padding(vertical = 8.dp)
					)
					TextBox(
						placeholder = stringResource(id = R.string.placeholder),
						value = task.content,
						onValueChange = ::onEnterKeyPress,
						textDecoration = if (task.done) TextDecoration.LineThrough else null,
						readOnly = note.trashed,
						modifier = Modifier
							.weight(weight = 1f)
							.padding(vertical = 4.dp)
							.onFocusChanged { showRemoveTaskButton = it.isFocused }
							.onKeyEvent { event -> onDeleteKeyPress(event) }
					)
					if (showRemoveTaskButton && note.trashed.not()) {
						IconButton(
							onClick = ::removeTask,
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
		}
		Task.Footer -> {
			if (note.trashed.not()) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.fillMaxWidth()
						.padding(all = 4.dp)
						.clickable(onClick = ::addTask)
				) {
					Icon(
						imageVector = Icons.Outlined.Add,
						contentDescription = null,
						modifier = Modifier.padding(start = 40.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
					)
					Text(
						text = stringResource(id = R.string.new_todo_item),
						modifier = Modifier.padding(top = 8.dp, end = 16.dp, bottom = 8.dp)
					)
				}
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
				Icon(
					imageVector = Icons.Outlined.DragIndicator,
					contentDescription = stringResource(id = R.string.drag_item),
					modifier = Modifier.padding(start = 8.dp, end = 2.dp)
				)
				Checkbox(
					checked = false,
					onCheckedChange = {},
					modifier = Modifier.padding(vertical = 8.dp)
				)
				TextBox(
					placeholder = stringResource(id = R.string.placeholder),
					value = "",
					onValueChange = {},
					modifier = Modifier
						.weight(weight = 1f)
						.padding(vertical = 4.dp)
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
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(all = 4.dp)
					.clickable(onClick = {})
			) {
				Icon(
					imageVector = Icons.Outlined.Add,
					contentDescription = null,
					modifier = Modifier.padding(start = 40.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
				)
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