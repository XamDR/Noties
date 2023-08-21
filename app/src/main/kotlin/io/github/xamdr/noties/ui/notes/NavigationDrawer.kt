package io.github.xamdr.noties.ui.notes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.media.MediaHelper
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import io.github.xamdr.noties.ui.theme.NotiesTheme
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawer(
	drawerState: DrawerState,
	preferenceStorage: PreferenceStorage,
	content: @Composable () -> Unit
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	var wallpaperUri by remember { mutableStateOf<Uri?>(Uri.parse(preferenceStorage.wallpaper)) }

	fun setWallpaper(uri: Uri?) {
		scope.launch {
			if (uri != null) {
				wallpaperUri = MediaHelper.copyUri(context, uri)
				preferenceStorage.wallpaper = wallpaperUri.toString()
			}
		}
	}

	val launcher = rememberLauncherForActivityResult(
		contract = PickVisualMedia(),
		onResult = ::setWallpaper
	)

	ModalNavigationDrawer(
		modifier = Modifier,
		drawerState = drawerState,
		drawerContent = {
			ModalDrawerSheet {
				NavigationHeader(src = wallpaperUri) {
					launcher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
				}
			}
		},
		content = content
	)
}

@Composable
private fun NavigationDrawer() {
	ModalNavigationDrawer(
		drawerState = DrawerState(DrawerValue.Open),
		drawerContent = {
			ModalDrawerSheet {
				NavigationHeader()
			}
		},
		content = {}
	)
}

@DevicePreviews
@Composable
private fun NavigationDrawerPreview() = NotiesTheme { NavigationDrawer() }

@Composable
private fun NavigationHeader(src: Any?, onSetWallpaper: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(176.dp)
	) {
		AsyncImage(
			contentScale = ContentScale.Crop,
			model = ImageRequest.Builder(LocalContext.current)
				.data(src)
				.build(),
			contentDescription = stringResource(id = R.string.user_wallpaper)
		)
		IconButton(
			onClick = onSetWallpaper,
			modifier = Modifier
				.align(Alignment.BottomStart)
				.padding(8.dp)
				.alpha(0.5f)
		) {
			Icon(
				imageVector = Icons.Outlined.Wallpaper,
				contentDescription = stringResource(id = R.string.set_wallpaper)
			)
		}
	}
}

@Composable
private fun NavigationHeader() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(176.dp)
	) {
		Image(
			imageVector = Icons.Outlined.Android,
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxWidth()
		)
		IconButton(
			onClick = {},
			modifier = Modifier
				.align(Alignment.BottomStart)
				.padding(8.dp)
				.alpha(0.5f)
		) {
			Icon(
				imageVector = Icons.Outlined.Wallpaper,
				contentDescription = stringResource(id = R.string.set_wallpaper)
			)
		}
	}
}