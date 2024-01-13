package io.github.xamdr.noties.ui.editor

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Gif
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.xamdr.noties.R
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.media.MediaHelper
import io.github.xamdr.noties.ui.helpers.shimmer
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun EditorGridItem(
	item: GridItem,
	onUriCopied: (MediaItem) -> Unit,
	onItemClick: () -> Unit,
	isFullWidth: Boolean
) {
	when (item) {
		is GridItem.AndroidUri -> {
			val context = LocalContext.current
			var mediaItem by remember { mutableStateOf<MediaItem?>(value = null) }

			LaunchedEffect(key1 = Unit) {
				mediaItem = convertUriToMediaItem(context, item.src)
				mediaItem?.let { onUriCopied(it) }
			}
			if (mediaItem == null) {
				Placeholder(isFullWidth = isFullWidth)
			}
		}
		is GridItem.Media -> EditorMediaItem(
			item = item.data,
			isFullWidth = isFullWidth,
			onItemClick = onItemClick
		)
	}
}

@Composable
private fun Placeholder(isFullWidth: Boolean) {
	val configuration = LocalConfiguration.current
	val modifier = if (isFullWidth) Modifier
		.fillMaxWidth()
		.heightIn(min = 200.dp, max = configuration.screenHeightDp.dp) else Modifier.size(200.dp)
	Box(
		modifier = modifier.shimmer(highlightColor = Color.DarkGray)
	)
}

private suspend fun convertUriToMediaItem(context: Context, uri: Uri): MediaItem {
	val newUri = MediaHelper.copyUri(context, uri)
	val mimeType = MediaHelper.getMediaMimeType(context, newUri)
	val mediaItem: MediaItem

	if (MediaHelper.isImage(context, newUri)) {
		mediaItem = MediaItem(
			uri = newUri,
			mimeType = mimeType,
			mediaType = MediaType.Image
		)
	}
	else {
		val metadata = MediaHelper.getMediaItemMetadata(context, newUri)
		mediaItem = MediaItem(
			uri = newUri,
			mimeType = mimeType,
			mediaType = MediaType.Video,
			metadata = metadata
		)
	}
	return mediaItem
}

@Composable
fun EditorGridItem() = EditorMediaItem()

@DevicePreviews
@Composable
private fun EditorGridItemPreview() = NotiesTheme { EditorGridItem() }

@Composable
private fun EditorMediaItem(item: MediaItem, isFullWidth: Boolean, onItemClick: () -> Unit) {
	val context = LocalContext.current
	val modifier = if (isFullWidth) Modifier.fillMaxWidth() else Modifier.size(200.dp)
	val isGif = MediaHelper.getMediaMimeType(context, item.uri) == Constants.GIF

	Box(
		modifier = Modifier.fillMaxWidth(),
		contentAlignment = Alignment.BottomStart
	) {
		AsyncImage(
			contentScale = ContentScale.Crop,
			model = ImageRequest.Builder(context)
				.data(
					if (item.mediaType == MediaType.Image) item.uri
					else item.metadata.thumbnail ?: R.drawable.ic_image_not_supported
				)
				.size(200, 200)
				.build(),
			contentDescription = stringResource(id = R.string.user_generated_image),
			modifier = modifier.clickable(onClick = onItemClick)
		)
		if (item.mediaType == MediaType.Video || isGif) {
			Box(
				modifier = Modifier.padding(8.dp)
			) {
				Row(
					modifier = Modifier.background(
						color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
						shape = RoundedCornerShape(8.dp)
					)
				) {
					if (isGif) {
						Icon(
							imageVector = Icons.Outlined.Gif,
							contentDescription = null,
							modifier = Modifier.padding(4.dp)
						)
					}
					else {
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