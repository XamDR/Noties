package io.github.xamdr.noties.ui.helpers

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize

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

fun Modifier.shimmer(highlightColor: Color): Modifier = this.composed {
	var size by remember { mutableStateOf(IntSize.Zero) }
	val transition = rememberInfiniteTransition(label = "transition")
	val startOffsetX by transition.animateFloat(
		initialValue = -2 * size.width.toFloat(),
		targetValue = 2 * size.width.toFloat(),
		animationSpec = infiniteRepeatable(animation = tween(1000)),
		label = "startOffsetX"
	)
	background(
		brush = Brush.linearGradient(
			colors = listOf(
				highlightColor.copy(alpha = 0.5f),
				highlightColor,
				highlightColor.copy(alpha = 0.5f),
			),
			start = Offset(startOffsetX, size.height.toFloat() / 2),
			end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat() / 2)
		)
	).onGloballyPositioned { size = it.size }
}
