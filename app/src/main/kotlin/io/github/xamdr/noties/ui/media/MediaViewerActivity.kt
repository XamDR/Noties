package io.github.xamdr.noties.ui.media

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.getParcelableArrayListCompat
import io.github.xamdr.noties.ui.theme.NotiesTheme

class MediaViewerActivity : AppCompatActivity() {

	private val items by lazy(LazyThreadSafetyMode.NONE) {
		intent.getParcelableArrayListCompat(Constants.BUNDLE_ITEMS, MediaItem::class.java)
	}
	private val position by lazy(LazyThreadSafetyMode.NONE) {
		intent.getIntExtra(Constants.BUNDLE_POSITION, 0)
	}
	private val itemsToDelete = mutableListOf<MediaItem>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent { MediaViewerActivityContent() }
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putParcelableArrayList(Constants.BUNDLE_ITEMS_DELETE, ArrayList(itemsToDelete))
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		val restoredItemsToDelete = savedInstanceState.getParcelableArrayListCompat(
			Constants.BUNDLE_ITEMS_DELETE,
			MediaItem::class.java
		)
		itemsToDelete.addAll(restoredItemsToDelete)
	}

	@Composable
	private fun MediaViewerActivityContent() {
		NotiesTheme {
			MediaViewerScreen(
				items = items,
				startIndex = position,
				window = window,
				onNavigationIconClick = ::navigateUp
			)
		}
	}

	private fun navigateUp() {
		onBackPressedDispatcher.addCallback(this) {
			val intent = Intent().apply {
				putExtra(Constants.BUNDLE_ITEMS_DELETE, ArrayList(itemsToDelete))
			}
			setResult(Activity.RESULT_OK, intent)
			finish()
		}
	}
}