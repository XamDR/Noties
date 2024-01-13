package io.github.xamdr.noties.ui.editor

import android.os.Build
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FormatColorReset
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

val editorLightColors = listOf(
	null,
	Color(0xCC1E88E5),
	Color(0xCCE53935),
	Color(0x99D81B60),
	Color(0x998E24AA),
	Color(0xCC00acc1),
	Color(0xCC00897B),
	Color(0xCC43A047),
	Color(0xCCFFFF00),
	Color(0x996D4C41),
	Color(0x99616161),
)

val editorDarkColors = listOf(
	null,
	Color(0x991E88E5),
	Color(0x99E53935),
	Color(0xCCD81B60),
	Color(0xCC8E24AA),
	Color(0x8C00acc1),
	Color(0x9900897B),
	Color(0x9943A047),
	Color(0x8CF57C00),
	Color(0xCC6D4C41),
	Color(0xCC616161),
)

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
	val modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Modifier
		.navigationBarsPadding()
		.padding(horizontal = 8.dp) else Modifier.padding(start = 8.dp, end = 8.dp, bottom = 32.dp)

	fun selectColor(color: Color?) {
		selectedColor = color
		onColorSelected(color)
	}

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState
	) {
		Row(
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
			modifier = modifier.horizontalScroll(state = rememberScrollState())
		) {
			colors.forEach { color ->
				CircularShapedBox(
					color = color ?: MaterialTheme.colorScheme.surface,
					size = 48.dp,
					modifier = Modifier.padding(all = 8.dp),
					onClick = { selectColor(color) }
				) {
					if (color == null || selectedColor == color) {
						Icon(
							imageVector = if (selectedColor == color) Icons.Outlined.Check else Icons.Outlined.FormatColorReset,
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
			editorColor = Color.Blue,
			sheetState = SheetState(skipPartiallyExpanded = true, initialValue = SheetValue.Expanded),
			onDismiss = {},
			onColorSelected = {}
		)
	}
}