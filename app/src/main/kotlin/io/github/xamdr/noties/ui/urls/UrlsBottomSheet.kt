package io.github.xamdr.noties.ui.urls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.notes.NotesViewModel
import io.github.xamdr.noties.ui.theme.NotiesTheme

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