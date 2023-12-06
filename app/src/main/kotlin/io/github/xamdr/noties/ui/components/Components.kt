package io.github.xamdr.noties.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.media.ActionItem
import io.github.xamdr.noties.ui.theme.NotiesTheme
import kotlin.math.max

@Composable
fun EmptyView(icon: ImageVector, @StringRes message: Int) {
	val iconSize = if (LocalConfiguration.current.screenHeightDp >= 600) 128.dp else 64.dp
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			modifier = Modifier.size(iconSize)
		)
		Text(text = stringResource(id = message))
	}
}

@DevicePreviews
@Composable
private fun EmptyViewPreview() {
	NotiesTheme {
		EmptyView(
			icon = Icons.Outlined.Android,
			message = R.string.app_name
		)
	}
}

@Composable
fun RadioButtonGroup(items: List<String>, selectedItem: String, onClick: (String) -> Unit) {
	Column(modifier = Modifier.selectableGroup()) {
		items.forEach { item ->
			Row(
				Modifier
					.fillMaxWidth()
					.height(48.dp)
					.selectable(
						selected = item == selectedItem,
						onClick = { onClick(item) },
						role = Role.RadioButton
					)
					.padding(horizontal = 8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				RadioButton(
					selected = item == selectedItem,
					onClick = null
				)
				Text(
					text = item,
					style = MaterialTheme.typography.bodyLarge,
					modifier = Modifier.padding(start = 8.dp)
				)
			}
		}
	}
}

@DevicePreviews
@Composable
private fun RadioButtonGroupPreview() {
	val items = listOf(
		"First option",
		"Second option",
		"Third option",
		"Fourth option",
	)
	NotiesTheme {
		RadioButtonGroup(
			items = items,
			selectedItem = items[0],
			onClick = {}
		)
	}
}

@Composable
fun CircularShapedBox(
	color: Color,
	size: Dp,
	modifier: Modifier = Modifier,
	onClick: () -> Unit = {},
	content: @Composable BoxScope.() -> Unit = {},
) {
	Box(
		modifier = modifier
			.clip(CircleShape)
			.size(size)
			.background(color = color, shape = CircleShape)
			.clickable(onClick = onClick),
		contentAlignment= Alignment.Center,
		content = content
	)
}

@DevicePreviews
@Composable
private fun CircularShapedBoxPreview() {
	CircularShapedBox(color = Color.Blue, size = 48.dp) {
		Icon(
			imageVector = Icons.Outlined.Check,
			contentDescription = null,
			modifier = Modifier.size(32.dp)
		)
	}
}

@Composable
fun TextBox(
	placeholder: String,
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	textDecoration: TextDecoration? = null
) {
	OutlinedTextField(
		value = value,
		onValueChange = onValueChange,
		modifier = modifier,
		placeholder = { Text(text = placeholder) },
		singleLine = false,
		keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
		colors = OutlinedTextFieldDefaults.colors(
			focusedBorderColor = Color.Transparent,
			unfocusedBorderColor = Color.Transparent
		),
		textStyle = if (textDecoration == null) LocalTextStyle.current
			else LocalTextStyle.current.copy(textDecoration = textDecoration)
	)
}

@DevicePreviews
@Composable
private fun TextBoxPreview() {
	NotiesTheme {
		TextBox(
			placeholder = stringResource(id = R.string.placeholder),
			value = "",
			onValueChange = {},
			modifier = Modifier.fillMaxWidth()
		)
	}
}

@Composable
fun OverflowMenu(items: List<ActionItem>) {
	var overflowMenuExpanded by rememberSaveable { mutableStateOf(false) }

	Box(
		modifier = Modifier.wrapContentSize()
	) {
		IconButton(onClick = { overflowMenuExpanded = true }) {
			Icon(
				imageVector = Icons.Outlined.MoreVert,
				contentDescription = stringResource(id = R.string.more_options)
			)
		}
		DropdownMenu(
			expanded = overflowMenuExpanded,
			onDismissRequest = { overflowMenuExpanded = false },
		) {
			for (item in items) {
				key(item.hashCode()) {
					DropdownMenuItem(
						text = { Text(text = stringResource(id = item.title)) },
						onClick = {
							overflowMenuExpanded = false
							item.action()
						},
						leadingIcon = {
							item.icon?.let {
								Icon(imageVector = it, contentDescription = null)
							}
						},
						modifier = Modifier.padding(start = 8.dp, top = 0.dp, bottom = 0.dp, end = 32.dp)
					)
				}
			}
		}
	}
}

@DevicePreviews
@Composable
private fun OverflowMenuPreview() {
	val items = listOf(
		ActionItem(title = R.string.copy_image, action = {}, icon = Icons.Outlined.Android)
	)
	NotiesTheme {
		for (item in items) {
			key(item.hashCode()) {
				DropdownMenuItem(
					text = { Text(text = stringResource(id = item.title)) },
					onClick = { item.action() },
					leadingIcon = { Icon(imageVector = item.icon!!, contentDescription = null) }
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
	onDismissRequest: () -> Unit,
	confirmButton: @Composable () -> Unit,
	modifier: Modifier = Modifier,
	dismissButton: @Composable (() -> Unit)? = null,
	tonalElevation: Dp = DatePickerDefaults.TonalElevation,
	properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
	content: @Composable ColumnScope.() -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismissRequest,
		modifier = modifier.wrapContentHeight(),
		properties = properties
	) {
		Surface(
			modifier = Modifier
				.requiredWidth(360.0.dp)
				.heightIn(max = 568.0.dp)
				.background(
					shape = MaterialTheme.shapes.extraLarge,
					color = MaterialTheme.colorScheme.surface
				),
			shape = MaterialTheme.shapes.extraLarge,
			tonalElevation = tonalElevation
		) {
			Column(
				modifier = Modifier.padding(24.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				content()
				// Buttons
				Box(
					modifier = Modifier
						.align(Alignment.End)
						.padding(PaddingValues(bottom = 8.dp, end = 6.dp))
				) {
					CompositionLocalProvider(
						LocalContentColor provides MaterialTheme.colorScheme.primary
					) {
						val textStyle = MaterialTheme.typography.labelMedium
						ProvideTextStyle(value = textStyle) {
							AlertDialogFlowRow(
								mainAxisSpacing = 8.dp,
								crossAxisSpacing = 12.dp
							) {
								dismissButton?.invoke()
								confirmButton()
							}
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@DevicePreviews
@Composable
private fun TimePickerDialogPreview() {
	NotiesTheme {
		TimePickerDialog(
			onDismissRequest = {},
			confirmButton = {}
		) {
			TimePicker(state = rememberTimePickerState())
		}
	}
}

@Composable
private fun AlertDialogFlowRow(
	mainAxisSpacing: Dp,
	crossAxisSpacing: Dp,
	content: @Composable () -> Unit
) {
	Layout(content) { measurables, constraints ->
		val sequences = mutableListOf<List<Placeable>>()
		val crossAxisSizes = mutableListOf<Int>()
		val crossAxisPositions = mutableListOf<Int>()
		var mainAxisSpace = 0
		var crossAxisSpace = 0
		val currentSequence = mutableListOf<Placeable>()
		var currentMainAxisSize = 0
		var currentCrossAxisSize = 0

		// Return whether the placeable can be added to the current sequence.
		fun canAddToCurrentSequence(placeable: Placeable) =
			currentSequence.isEmpty() || currentMainAxisSize + mainAxisSpacing.roundToPx() +
					placeable.width <= constraints.maxWidth

		// Store current sequence information and start a new sequence.
		fun startNewSequence() {
			if (sequences.isNotEmpty()) {
				crossAxisSpace += crossAxisSpacing.roundToPx()
			}
			sequences += currentSequence.toList()
			crossAxisSizes += currentCrossAxisSize
			crossAxisPositions += crossAxisSpace

			crossAxisSpace += currentCrossAxisSize
			mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)

			currentSequence.clear()
			currentMainAxisSize = 0
			currentCrossAxisSize = 0
		}

		for (measurable in measurables) {
			// Ask the child for its preferred size.
			val placeable = measurable.measure(constraints)

			// Start a new sequence if there is not enough space.
			if (!canAddToCurrentSequence(placeable)) startNewSequence()

			// Add the child to the current sequence.
			if (currentSequence.isNotEmpty()) {
				currentMainAxisSize += mainAxisSpacing.roundToPx()
			}
			currentSequence.add(placeable)
			currentMainAxisSize += placeable.width
			currentCrossAxisSize = max(currentCrossAxisSize, placeable.height)
		}

		if (currentSequence.isNotEmpty()) startNewSequence()

		val mainAxisLayoutSize = max(mainAxisSpace, constraints.minWidth)

		val crossAxisLayoutSize = max(crossAxisSpace, constraints.minHeight)

		layout(mainAxisLayoutSize, crossAxisLayoutSize) {
			sequences.forEachIndexed { i, placeables ->
				val childrenMainAxisSizes = IntArray(placeables.size) { j ->
					placeables[j].width +
							if (j < placeables.lastIndex) mainAxisSpacing.roundToPx() else 0
				}
				val arrangement = Arrangement.End
				val mainAxisPositions = IntArray(childrenMainAxisSizes.size) { 0 }
				with(arrangement) {
					arrange(
						mainAxisLayoutSize, childrenMainAxisSizes,
						layoutDirection, mainAxisPositions
					)
				}
				placeables.forEachIndexed { j, placeable ->
					placeable.place(
						x = mainAxisPositions[j],
						y = crossAxisPositions[i]
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropDown(
	hint: String,
	value: String,
	items: List<T>,
	entries: Array<String>,
	modifier: Modifier = Modifier,
	onItemClick: (T) -> Unit
) {
	var expanded by rememberSaveable { mutableStateOf(value = false) }

	ExposedDropdownMenuBox(
		expanded = expanded,
		onExpandedChange = { expanded = !expanded },
		modifier = modifier
	) {
		OutlinedTextField(
			readOnly = true,
			value = value,
			onValueChange = {},
			placeholder = { Text(text = hint) },
			singleLine = true,
			trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
			modifier = Modifier.menuAnchor()
		)
		if (items.isNotEmpty()) {
			ExposedDropdownMenu(
				expanded = expanded,
				onDismissRequest = { expanded = false }
			) {
				items.forEachIndexed { index, item ->
					DropdownMenuItem(
						text = { Text(text = entries[index]) },
						onClick = {
							expanded = false
							onItemClick(item)
						},
						modifier = Modifier.padding(ExposedDropdownMenuDefaults.ItemContentPadding)
					)
				}
			}
		}
	}
}

@DevicePreviews
@Composable
private fun DropDownPreview() {
	NotiesTheme {
		DropDown(
			hint = "Fecha",
			value = "",
			items = emptyList<Unit>(),
			entries = emptyArray(),
			onItemClick = {},
			modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
		)
	}
}