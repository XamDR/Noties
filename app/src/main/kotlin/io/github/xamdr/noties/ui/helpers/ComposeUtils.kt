package io.github.xamdr.noties.ui.helpers

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalWindowInfo

fun Modifier.showSoftKeyboardOnFocus(focusRequester: FocusRequester): Modifier {
	return this.focusRequester(focusRequester).then(composed {
		val windowInfo = LocalWindowInfo.current
		LaunchedEffect(windowInfo) {
			snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
				if (isWindowFocused) {
					focusRequester.requestFocus()
				}
			}
		}
		this@showSoftKeyboardOnFocus
	})
}