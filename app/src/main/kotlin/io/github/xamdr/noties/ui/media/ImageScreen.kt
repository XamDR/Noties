package io.github.xamdr.noties.ui.media

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import coil.load
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.media.MediaHelper
import io.github.xamdr.noties.ui.theme.NotiesTheme
import pl.droidsonroids.gif.GifDrawable

@Composable
fun ImageScreen(
	item: MediaItem,
	onClick: () -> Unit,
	onZoom: (Boolean) -> Unit
) {
	Box {
		PhotoImageView(
			src = item.uri,
			onClick = onClick,
			onZoom = onZoom
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

@Composable
fun PhotoImageView(
	src: Uri,
	onClick: () -> Unit,
	onZoom: (Boolean) -> Unit
) {
	AndroidView(
		modifier = Modifier.fillMaxSize(),
		factory = { context ->
			PhotoImageView(context).apply {
				val mimetype = MediaHelper.getMediaMimeType(context, src)
				if (mimetype != null && mimetype != Constants.GIF) {
					load(src)
				}
				else {
					val gifDrawable = GifDrawable(context.contentResolver, src)
					setImageDrawable(gifDrawable)
				}
				adjustViewBounds = true
				contentDescription = context.getString(R.string.user_generated_image)
				scaleType = ImageView.ScaleType.FIT_CENTER
				setOnClickListener { onClick() }
				setOnTouchImageListener { onZoom(isZoomed) }
			}
		}
	)
}