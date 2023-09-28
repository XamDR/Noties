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
import coil.request.ImageRequest
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.clickableWithoutRipple
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun ImageScreen(
	item: MediaItem,
	onClick: () -> Unit
) {
	Box(modifier = Modifier.clickableWithoutRipple(onClick = onClick)) {
		AsyncImage(
			model = ImageRequest.Builder(LocalContext.current)
				.data(item.uri)
				.build(),
			contentDescription = stringResource(id = R.string.user_generated_image),
			modifier = Modifier.fillMaxSize(),
			alignment = Alignment.Center,
			contentScale = ContentScale.Fit
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