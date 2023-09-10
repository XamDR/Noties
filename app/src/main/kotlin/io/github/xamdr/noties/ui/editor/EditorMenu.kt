package io.github.xamdr.noties.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorMenuBottomSheet(sheetState: SheetState) {
	ModalBottomSheet(
		onDismissRequest = {},
		sheetState = sheetState,
		dragHandle = {
			Divider(
				modifier = Modifier
					.fillMaxWidth(0.2f)
					.padding(8.dp),
				thickness = 4.dp
			)
		}
	) {
		EditorMenu()
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@DevicePreviews
@Composable
private fun EditorMenuBottomSheetPreview() {
	NotiesTheme {
		EditorMenuBottomSheet(
			sheetState = SheetState(
				skipPartiallyExpanded = true,
				initialValue = SheetValue.Expanded
			)
		)
	}
}

@Composable
private fun EditorMenu() {
	Column {
		EDITOR_MENU_ITEMS.forEach { item ->
			EditorMenuItem(item = item, onClick = {})
		}
	}
}

@Composable
private fun EditorMenuItem(item: EditorMenuItem, onClick: (EditorMenuItem) -> Unit) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(16.dp),
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp)
			.clickable { onClick(item) }
	) {
		Icon(imageVector = item.icon, contentDescription = null)
		Text(
			text = stringResource(id = item.description),
			style = MaterialTheme.typography.titleMedium,
			modifier = Modifier.weight(1f)
		)
	}
}