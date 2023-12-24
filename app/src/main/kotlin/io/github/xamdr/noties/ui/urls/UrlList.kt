package io.github.xamdr.noties.ui.urls

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.components.OverflowMenu
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.media.ActionItem
import io.github.xamdr.noties.ui.notes.NotesViewModel
import io.github.xamdr.noties.ui.theme.NotiesTheme
import io.github.xamdr.noties.domain.model.UrlItem as Url

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlsBottomSheet(
	sources: List<String>,
	sheetState: SheetState,
	onDismiss: () -> Unit,
	viewModel: NotesViewModel
) {
	val uriHandler = LocalUriHandler.current
	val urls by viewModel.getUrls(sources).collectAsStateWithLifecycle(initialValue = emptyList())
	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState
	) {
		if (urls.isEmpty()) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 8.dp),
				contentAlignment = Alignment.Center
			) {
				CircularProgressIndicator()
			}
		}
		else {
			UrlList(urls = urls) { source ->
				uriHandler.openUri(source)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UrlsBottomSheet(
	sheetState: SheetState,
	onDismiss: () -> Unit
) {
	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState
	) {
		UrlList()
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@DevicePreviews
@Composable
private fun UrlsBottomSheetPreview() {
	NotiesTheme {
		UrlsBottomSheet(
			sheetState = SheetState(skipPartiallyExpanded = true, initialValue = SheetValue.Expanded),
			onDismiss = {}
		)
	}
}

@Composable
private fun UrlList(
	urls: List<Url>,
	onUrlClick: (String) -> Unit,
) {
	LazyColumn(
		contentPadding = PaddingValues(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		items(items = urls, key = { url -> url.id }) { url ->
			UrlItem(url = url, onItemClick = onUrlClick)
		}
	}
}

@Composable
private fun UrlList(size: Int = 5) {
	LazyColumn(
		contentPadding = PaddingValues(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		items(count = size) { UrlItem() }
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlItem(
	url: Url,
	onItemClick: (String) -> Unit,
	hasMenu: Boolean = false,
	onCopyUrl: ((String) -> Unit)? = null,
	onDeleteUrl: ((String) -> Unit)? = null
) {
	Card(
		onClick = { onItemClick(url.source) },
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(all = 4.dp)
		) {
			if (url.metadata.image != null) {
				AsyncImage(
					model = url.metadata.image,
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = Modifier.size(size = 48.dp)
				)
			}
			else {
				Icon(
					imageVector = Icons.Outlined.Language,
					contentDescription = null,
					modifier = Modifier.size(size = 48.dp)
				)
			}
			Column(
				modifier = Modifier.weight(weight = 1f)
			) {
				Text(
					text = url.metadata.title ?: stringResource(R.string.url_without_title_metadata),
					modifier = Modifier.padding(horizontal = 8.dp),
					style = MaterialTheme.typography.bodyLarge
				)
				Text(
					text = url.metadata.host ?: stringResource(R.string.url_without_host_metadata),
					modifier = Modifier.padding(horizontal = 8.dp),
					style = MaterialTheme.typography.labelMedium
				)
			}
			if (hasMenu) {
				OverflowMenu(
					items = listOf(
						ActionItem(title = R.string.copy_image, action = { onCopyUrl?.invoke(url.source) }),
						ActionItem(title = R.string.delete_item, action = { onDeleteUrl?.invoke(url.source) })
					)
				)
			}
		}
	}
}

@Composable
private fun UrlItem() {
	NotiesTheme {
		Card(
			modifier = Modifier.fillMaxWidth()
		) {
			Row(
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.padding(all = 4.dp)
			) {
				Image(
					imageVector = Icons.Outlined.Language,
					contentDescription = null,
					modifier = Modifier.padding(horizontal = 16.dp)
				)
				Column(
					modifier = Modifier.weight(weight = 1f)
				) {
					Text(
						text = "ExtraEmily - Twitch",
						modifier = Modifier.padding(all = 2.dp),
						style = MaterialTheme.typography.bodyLarge
					)
					Text(
						text = "www.twitch.tv",
						modifier = Modifier.padding(all = 2.dp),
						style = MaterialTheme.typography.labelMedium
					)
				}
				IconButton(onClick = {}) {
					Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = null)
				}
			}
		}
	}
}
