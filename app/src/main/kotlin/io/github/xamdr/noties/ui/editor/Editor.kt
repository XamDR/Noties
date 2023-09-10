package io.github.xamdr.noties.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.xamdr.noties.R
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.components.TextBox
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.media.MediaHelper
import io.github.xamdr.noties.ui.theme.NotiesTheme

private const val SPAN_COUNT = 2

@Composable
fun Editor(
	modifier: Modifier,
	note: Note,
	onNoteContentChange: (String) -> Unit
) {
	val items = note.items
	LazyVerticalGrid(
		modifier = modifier,
		contentPadding = PaddingValues(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		columns = GridCells.Fixed(count = SPAN_COUNT)
	) {
		items(
			count = items.size,
			key = { index -> items[index].id },
			span = { index ->
				val span = if (items.size.mod(SPAN_COUNT) == 1 && index == 0) SPAN_COUNT else 1
				GridItemSpan(span)
			},
			itemContent = { index -> EditorMediaItem(item = items[index]) }
		)
		item(span = { GridItemSpan(SPAN_COUNT) }) {
			TextBox(
				placeholder = stringResource(id = R.string.placeholder),
				value = note.text,
				onValueChange = onNoteContentChange,
				modifier = Modifier.fillMaxWidth()
			)
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
			itemContent = { EditorMediaItem() }
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

@Composable
fun EditorMediaItem(item: MediaItem) {
	Box(
		modifier = Modifier.fillMaxWidth(),
		contentAlignment = Alignment.TopEnd
	) {
		AsyncImage(
			contentScale = ContentScale.Crop,
			model = ImageRequest.Builder(LocalContext.current)
				.data(
					if (item.mediaType == MediaType.Image) item.uri
					else item.metadata.thumbnail ?: R.drawable.ic_image_not_supported
				)
				.build(),
			contentDescription = stringResource(id = R.string.user_generated_image)
		)
		if (item.mediaType == MediaType.Video) {
			Box(
				modifier = Modifier.padding(8.dp)
			) {
				Row(
					modifier = Modifier.background(
						color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
						shape = RoundedCornerShape(8.dp)
					)
				) {
					Icon(
						imageVector = Icons.Outlined.PlayCircle,
						contentDescription = null,
						modifier = Modifier.padding(4.dp)
					)
					Text(
						text = MediaHelper.formatDuration(item.metadata.duration),
						style = MaterialTheme.typography.labelSmall,
						fontWeight = FontWeight.Bold,
						modifier = Modifier
							.align(Alignment.CenterVertically)
							.padding(end = 8.dp)
					)
				}
			}
		}
	}
}

@Composable
private fun EditorMediaItem() {
	Box(
		contentAlignment = Alignment.TopEnd
	) {
		Image(
			imageVector = Icons.Outlined.Android,
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxWidth()
		)
		Box(
			modifier = Modifier.padding(8.dp)
		) {
			Row(
				modifier = Modifier.background(
					color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
					shape = RoundedCornerShape(8.dp)
				)
			) {
				Icon(
					imageVector = Icons.Outlined.PlayCircle,
					contentDescription = null,
					modifier = Modifier.padding(4.dp)
				)
				Text(
					text = "04:40",
					style = MaterialTheme.typography.labelSmall,
					fontWeight = FontWeight.Bold,
					modifier = Modifier
						.align(Alignment.CenterVertically)
						.padding(end = 8.dp)
				)
			}
		}
	}
}