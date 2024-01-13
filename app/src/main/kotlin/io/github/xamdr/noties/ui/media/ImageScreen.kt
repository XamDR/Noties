package io.github.xamdr.noties.ui.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.media.MediaHelper
import io.github.xamdr.noties.ui.theme.NotiesTheme
import pl.droidsonroids.gif.GifDrawable

@Suppress("IMPLICIT_CAST_TO_ANY")
@Composable
fun ImageScreen(
	item: MediaItem,
	onClick: () -> Unit
) {
	val context = LocalContext.current
	val mimeType = MediaHelper.getMediaMimeType(context, item.uri)
	val model = if (mimeType != null && mimeType != Constants.GIF) item.uri else GifDrawable(context.contentResolver, item.uri)
	val zoomState = rememberZoomState(maxScale = 3f)

	Box {
		AsyncImage(
			model = model,
			contentDescription = stringResource(id = R.string.user_generated_image),
			alignment = Alignment.Center,
			contentScale = ContentScale.Fit,
			modifier = Modifier
				.fillMaxSize()
				.zoomable(
					zoomState = zoomState,
					onTap = { onClick() },
					onDoubleTap = { offset -> zoomState.toggleScale(2f, offset) }
				),
			onSuccess = { state ->
				zoomState.setContentSize(state.painter.intrinsicSize)
			}
		)
	}
}

@Composable
private fun ImageScreen() {
	Box {
		Image(
			imageVector = Icons.Outlined.Android,
			contentDescription = null,
			modifier = Modifier.fillMaxSize(),
			alignment = Alignment.Center,
			contentScale = ContentScale.Fit
		)
	}
}

@DevicePreviews
@Composable
private fun ImageScreenPreview() = NotiesTheme { ImageScreen() }
