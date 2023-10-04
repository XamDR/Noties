package io.github.xamdr.noties.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.media.ActionItem
import io.github.xamdr.noties.ui.theme.NotiesTheme

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
	modifier: Modifier
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
		)
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
						leadingIcon = { Icon(imageVector = item.icon, contentDescription = null) },
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
					leadingIcon = { Icon(imageVector = item.icon, contentDescription = null) }
				)
			}
		}
	}
}