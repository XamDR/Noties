package io.github.xamdr.noties.ui.editor

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
fun EditorMenuBottomSheet(
	items: List<EditorMenuItem>,
	sheetState: SheetState,
	onDismissRequest: () -> Unit,
	onItemClick: (EditorMenuItem) -> Unit,
) {
	val modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Modifier.navigationBarsPadding()
		else Modifier.padding(bottom = 32.dp)

	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetState = sheetState
	) {
		Column(modifier = modifier) {
			items.forEach { item ->
				EditorMenuItem(
					item = item,
					onClick = { onItemClick(item) }
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorMenuBottomSheet() {
	ModalBottomSheet(
		onDismissRequest = {},
		sheetState = SheetState(
			skipPartiallyExpanded = true,
			initialValue = SheetValue.Expanded
		)
	) {
		Column {
			EDITOR_MENU_ITEMS_NORMAL.forEach { item ->
				EditorMenuItem(item = item, onClick = {})
			}
		}
	}
}

@DevicePreviews
@Composable
private fun EditorMenuBottomSheetPreview() = NotiesTheme { EditorMenuBottomSheet() }

@Composable
private fun EditorMenuItem(item: EditorMenuItem, onClick: () -> Unit) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(16.dp),
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 8.dp)
			.clickable(onClick = onClick)
	) {
		Icon(
			imageVector = item.icon,
			contentDescription = null,
			modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)
		)
		Text(
			text = stringResource(id = item.description),
			style = MaterialTheme.typography.titleMedium,
			modifier = Modifier
				.weight(1f)
				.padding(start = 4.dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
		)
	}
}