package io.github.xamdr.noties.ui.notes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
	viewModel: NotesViewModel,
	preferenceStorage: PreferenceStorage,
	navController: NavController,
	onCreateTag: () -> Unit,
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

	val tags by viewModel.getTags().collectAsState(initial = emptyList())
	val items = DEFAULT_DRAWER_ITEMS.toMutableStateList().apply {
		tags.mapIndexed { index, tag ->
			add(index + 4, DrawerItem.TagItem(
				id = tag.id,
				icon = Icons.Outlined.Label,
				label = tag.name
			))
		}
	}
	var selectedItem by remember { mutableStateOf(items[0]) }

	ModalNavigationDrawer(
		modifier = Modifier,
		drawerState = drawerState,
		drawerContent = {
			ModalDrawerSheet {
				DrawerHeader(src = wallpaperUri) {
					launcher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
				}
				LazyColumn(
					contentPadding = PaddingValues(16.dp),
					verticalArrangement = Arrangement.spacedBy(16.dp),
				) {
					items(
						count = items.size,
						key = { index -> items[index].id },
						contentType = { index -> items[index].javaClass }
					) { index ->
						val item = items[index]
						DrawerItem(
							item = item,
							selected = item == selectedItem
						) {
							selectedItem = item
							if (item.id == R.string.create_tag) {
								onCreateTag()
							}
							else {
								scope.launch {
									drawerState.close()
									when (item.id) {
										R.string.settings -> navController.navigate(R.id.action_notes_to_settings)
									}
								}
							}
						}
					}
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
				DrawerHeader()
				DEFAULT_DRAWER_ITEMS.forEach { item ->
					DrawerItem(
						item = item,
						selected = DEFAULT_DRAWER_ITEMS.indexOf(item) == 0,
						onClick = {}
					)
				}
			}
		},
		content = {}
	)
}

@DevicePreviews
@Composable
private fun NavigationDrawerPreview() = NotiesTheme { NavigationDrawer() }

@Composable
private fun DrawerHeader(src: Any?, onSetWallpaper: () -> Unit) {
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
private fun DrawerHeader() {
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

@Composable
private fun DrawerItem(item: DrawerItem, selected: Boolean, onClick: () -> Unit) {
	when (item) {
		is DrawerItem.DefaultItem -> {
			NavigationDrawerItem(
				icon = { Icon(imageVector = item.icon, contentDescription = item.icon.name) },
				label = { Text(text = stringResource(id = item.label)) },
				selected = selected,
				onClick = onClick,
				modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
			)
			if (item.label == R.string.create_tag) {
				Divider(modifier = Modifier.padding(8.dp))
			}
		}
		is DrawerItem.Header -> {
			Divider(modifier = Modifier.padding(8.dp))
			Text(
				text = stringResource(id = item.label),
				modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
			)
		}
		is DrawerItem.TagItem -> {
			NavigationDrawerItem(
				icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
				label = { Text(text = item.label) },
				selected = selected,
				onClick = onClick,
				modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
			)
		}
	}
}