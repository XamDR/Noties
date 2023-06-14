package io.github.xamdr.noties.ui.media

import android.os.Build
import android.view.View
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class FullScreenHelper(private val onEnterFullScreen: () -> Unit, private val onExitFullScreen: () -> Unit) {

	fun toggleSystemBarsVisibility(view: View, window: Window) {
		val windowInsetsController = WindowCompat.getInsetsController(window, view)
		windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
		ViewCompat.getRootWindowInsets(view)?.let { windowInsets ->
			val systemInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
			// We use systemInsets.top to check if the status bar is visible,
			// and systemInsets.bottom to check if the navigation bar is visible
			if (systemInsets.top > 0 || systemInsets.bottom > 0) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
					WindowCompat.setDecorFitsSystemWindows(window, false)
				}
				windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
				onEnterFullScreen()
			}
			else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
					WindowCompat.setDecorFitsSystemWindows(window, true)
				}
				windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
				onExitFullScreen()
			}
		}
	}
}