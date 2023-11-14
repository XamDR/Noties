package io.github.xamdr.noties.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.components.TextBox
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme

private const val SPAN_COUNT = 2

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Editor(
	modifier: Modifier,
	note: Note,
	items: List<GridItem>,
	tags: List<String>?,
	onNoteContentChange: (String) -> Unit,
	onItemCopied: (MediaItem, Int) -> Unit,
	onItemClick: (Int) -> Unit
) {
	LazyVerticalGrid(
		modifier = modifier,
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
		item(span = { GridItemSpan(SPAN_COUNT) }) {
			TextBox(
				placeholder = stringResource(id = R.string.placeholder),
				value = note.text,
				onValueChange = onNoteContentChange,
				modifier = Modifier.fillMaxWidth()
			)
		}
		item(span = { GridItemSpan(SPAN_COUNT) }) {
			if (!tags.isNullOrEmpty()) {
				FlowRow(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					tags.forEach { tag ->
						SuggestionChip(
							onClick = {},
							label = { Text(text = tag) }
						)
					}
				}
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