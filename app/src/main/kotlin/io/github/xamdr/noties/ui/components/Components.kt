package io.github.xamdr.noties.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.DevicePreviews
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