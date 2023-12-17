package io.github.xamdr.noties.ui.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FormatColorReset
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.ui.components.CircularShapedBox
import io.github.xamdr.noties.ui.helpers.ColorSaver
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorColorBottomSheet(
	colors: List<Color?>,
	editorColor: Color? = null,
	sheetState: SheetState,
	onColorSelected: (Color?) -> Unit,
	onDismiss: () -> Unit
) {
	var selectedColor by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(value = editorColor) }

	fun selectColor(color: Color?) {
		selectedColor = color
		onColorSelected(color)
		onDismiss()
	}

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState,
		containerColor = selectedColor ?: BottomSheetDefaults.ContainerColor
	) {
		Row(
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
				.horizontalScroll(state = rememberScrollState())
		) {
			colors.forEach { color ->
				CircularShapedBox(
					color = color ?: MaterialTheme.colorScheme.surfaceVariant,
					size = 48.dp,
					modifier = Modifier.padding(all = 8.dp),
					onClick = { selectColor(color) }
				) {
					if (selectedColor == color) {
						Icon(
							imageVector = if (selectedColor == null) Icons.Outlined.FormatColorReset else Icons.Outlined.Check,
							contentDescription = null,
							modifier = Modifier.padding(all = 2.dp)
						)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@DevicePreviews
@Composable
private fun EditorColorBottomSheetPreview() {
	NotiesTheme {
		EditorColorBottomSheet(
			colors = listOf(null, Color.Blue, Color.Red, Color.Green, Color.Yellow),
			sheetState = SheetState(skipPartiallyExpanded = true, initialValue = SheetValue.Expanded),
			onDismiss = {},
			onColorSelected = {}
		)
	}
}