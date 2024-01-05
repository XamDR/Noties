package io.github.xamdr.noties.ui.media

import android.view.View
import android.view.Window
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import io.github.xamdr.noties.R
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.components.ActionItem
import io.github.xamdr.noties.ui.components.OverflowMenu
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.findActivity
import io.github.xamdr.noties.ui.helpers.rememberMutableStateList
import io.github.xamdr.noties.ui.theme.NotiesTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MediaViewerScreen(
	items: List<MediaItem>,
	startIndex: Int,
	window: Window,
	onNavigationIconClick: () -> Unit,
	onItemDeleted: (MediaItem) -> Unit,
) {
	val mediaItems = rememberMutableStateList(*items.toTypedArray())
	val pagerState = rememberPagerState(
		initialPage = startIndex,
		pageCount = { mediaItems.size }
	)
	val currentItem = mediaItems[pagerState.currentPage]
	val context = LocalContext.current
	val activity = context.findActivity() ?: return
	val view = LocalView.current
	val scope = rememberCoroutineScope()
	val imageActions = ImageActions(context, currentItem)
	val videoActions = VideoActions(context, currentItem)
	val snackbarHostState = remember { SnackbarHostState() }
	val errorPlaybackMsg = stringResource(id = R.string.error_video_playback)
	var isFullScreen by rememberSaveable { mutableStateOf(value = false) }
	var scrollEnabled by rememberSaveable { mutableStateOf(value = true) }

	fun toggleFullScreen() {
		if (isFullScreen) {
			exitFullScreen(view, window) { isFullScreen = !isFullScreen }
		}
		else {
			enterFullScreen(view, window) { isFullScreen = !isFullScreen }
		}
	}

	fun onPlayerError() {
		scope.launch { snackbarHostState.showSnackbar(errorPlaybackMsg) }
	}

	fun onDelete() {
		if (!mediaItems.isSingleton()) {
			val currentIndex = mediaItems.indexOf(currentItem)
			mediaItems.remove(currentItem)
			onItemDeleted(currentItem)

			// Scroll to the previous item, since by default the Pager will try to
			// scroll to the next item, which for the last element is nonexistent
			if (currentIndex == mediaItems.size) {
				scope.launch { pagerState.scrollToPage(currentIndex - 1) }
			}
		}
		else {
			onItemDeleted(currentItem)
			activity.onBackPressedDispatcher.onBackPressed()
		}
	}

	val overflowItems = when (currentItem.mediaType) {
		MediaType.Image -> getOverflowItemsForImage(currentItem, imageActions, scope, ::onDelete)
		MediaType.Video -> getOverflowItemsForVideo(currentItem, videoActions, scope, ::onDelete)
		MediaType.Audio -> throw IllegalArgumentException("Invalid media type: ${currentItem.mediaType}")
	}

	Scaffold(
		snackbarHost = { SnackbarHost(snackbarHostState) },
		content = { innerPadding ->
			Box {
				HorizontalPager(
					state = pagerState,
					key = { index -> mediaItems[index].uri },
					userScrollEnabled = scrollEnabled
				) { index ->
					when (mediaItems[index].mediaType) {
						MediaType.Image -> ImageScreen(
							item = mediaItems[index],
							onClick = ::toggleFullScreen,
							onZoom = { isZoomed -> scrollEnabled = isZoomed.not() }
						)
						MediaType.Video -> VideoScreen(
							item = mediaItems[index],
							playWhenReady = index == pagerState.currentPage,
							window = window,
							onFullScreen = ::toggleFullScreen,
							onPlayerError = ::onPlayerError
						)
						MediaType.Audio -> {}
					}
				}
				AnimatedVisibility(
					visible = isFullScreen.not(),
					enter = slideInVertically(
						initialOffsetY = { fullHeight -> -fullHeight },
						animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing)
					),
					exit = slideOutVertically(
						targetOffsetY = { fullHeight -> -fullHeight },
						animationSpec = tween(durationMillis = 400, easing = FastOutLinearInEasing)
					)
				) {
					TopAppBar(
						windowInsets = WindowInsets(
							left = 0.dp,
							top = innerPadding.calculateTopPadding(),
							right = 0.dp,
							bottom = 0.dp
						),
						colors = TopAppBarDefaults.topAppBarColors(
							containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
						),
						title = { Text(text = "${pagerState.currentPage + 1}/${mediaItems.size}") },
						navigationIcon = {
							IconButton(onClick = onNavigationIconClick) {
								Icon(
									imageVector = Icons.Outlined.ArrowBack,
									contentDescription = stringResource(id = R.string.back_to_editor)
								)
							}
						},
						actions = {
							IconButton(onClick = { shareMediaItem(currentItem, context) }) {
								Icon(
									imageVector = Icons.Outlined.Share,
									contentDescription = stringResource(id = R.string.share_item)
								)
							}
							if (currentItem.mediaType == MediaType.Image) {
								IconButton(onClick = { toggleScreenOrientation(context, activity) }) {
									Icon(
										imageVector = Icons.Outlined.ScreenRotation,
										contentDescription = stringResource(id = R.string.rotate)
									)
								}
							}
							OverflowMenu(items = overflowItems)
						}
					)
				}
			}
		}
	)
}

private fun getOverflowItemsForImage(
	item: MediaItem,
	imageActions: MediaImageActions,
	scope: CoroutineScope,
	onDelete: () -> Unit,
): List<ActionItem> {
	return buildList {
		add(
			ActionItem(
				title = R.string.copy_image,
				action = imageActions::onCopy,
				icon = Icons.Outlined.ContentCopy
			)
		)
		add(
			ActionItem(
				title = R.string.download_item,
				action = { scope.launch { imageActions.onSave() } },
				icon = Icons.Outlined.SaveAlt
			)
		)
		if (item.trashed.not()) {
			add(
				ActionItem(
					title = R.string.delete_item,
					action = onDelete,
					icon = Icons.Outlined.DeleteForever
				)
			)
		}
		add(
			ActionItem(
				title = R.string.print_image,
				action = imageActions::onPrint,
				icon = Icons.Outlined.Print
			)
		)
		add(
			ActionItem(
				title = R.string.set_image_as,
				action = imageActions::onSetAs,
				icon = Icons.Outlined.Image
			)
		)
	}
}

private fun getOverflowItemsForVideo(
	item: MediaItem,
	videoActions: MediaActions,
	scope: CoroutineScope,
	onDelete: () -> Unit
): List<ActionItem> {
	return buildList {
		add(
			ActionItem(
				title = R.string.download_item,
				action = { scope.launch { videoActions.onSave() } },
				icon = Icons.Outlined.SaveAlt
			)
		)
		if (item.trashed.not()) {
			add(
				ActionItem(
					title = R.string.delete_item,
					action = onDelete,
					icon = Icons.Outlined.DeleteForever
				)
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MediaViewerScreen() {
	val pagerState = rememberPagerState(pageCount = { 1 })

	Box {
		HorizontalPager(state = pagerState) {
			Box(modifier = Modifier) {
				Image(
					imageVector = Icons.Outlined.Android,
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
		TopAppBar(
			title = { Text(text = "3/7") },
			navigationIcon = {
				IconButton(onClick = {}) {
					Icon(
						imageVector = Icons.Outlined.ArrowBack,
						contentDescription = stringResource(id = R.string.back_to_editor)
					)
				}
			},
			actions = {
				IconButton(onClick = {}) {
					Icon(
						imageVector = Icons.Outlined.Share,
						contentDescription = stringResource(id = R.string.share_item)
					)
				}
				IconButton(onClick = {}) {
					Icon(
						imageVector = Icons.Outlined.ScreenRotation,
						contentDescription = stringResource(id = R.string.rotate)
					)
				}
			}
		)
	}
}

@DevicePreviews
@Composable
private fun MediaViewerScreenPreview() = NotiesTheme { MediaViewerScreen() }

private fun enterFullScreen(view: View, window: Window, onEnterFullScreen: () -> Unit) {
	WindowCompat.getInsetsController(window, view).hide(WindowInsetsCompat.Type.systemBars())
	onEnterFullScreen()
}

private fun exitFullScreen(view: View, window: Window, onExitFullScreen: () -> Unit) {
	WindowCompat.getInsetsController(window, view).show(WindowInsetsCompat.Type.systemBars())
	onExitFullScreen()
}