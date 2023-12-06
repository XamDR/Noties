package io.github.xamdr.noties.ui.helpers

import android.text.Annotation
import android.text.SpannedString
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.core.text.getSpans
import io.github.xamdr.noties.domain.model.Note

fun Modifier.onFocusShowSoftKeyboard(focusRequester: FocusRequester): Modifier {
	return this
		.focusRequester(focusRequester)
		.then(composed {
			val windowInfo = LocalWindowInfo.current
			LaunchedEffect(windowInfo) {
				snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
					if (isWindowFocused) {
						focusRequester.requestFocus()
					}
				}
			}
			this@onFocusShowSoftKeyboard
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

fun Modifier.clickableWithoutRipple(onClick: () -> Unit) = composed(
	factory = {
		this.then(
			Modifier.clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = null,
				onClick = onClick
			)
		)
	}
)

@Composable
fun <T: Any> rememberMutableStateList(vararg elements: T): SnapshotStateList<T> {
	return rememberSaveable(saver = listSaver(
		save = { it.toList() },
		restore = { it.toMutableStateList() }
	)) {
		elements.toList().toMutableStateList()
	}
}

@Composable
fun annotatedStringResource(
	@StringRes id: Int,
	spanStyles: (Annotation) -> SpanStyle? = { null }
): AnnotatedString {
	val resources = LocalContext.current.resources
	val spannedString = SpannedString(resources.getText(id))
	val resultBuilder = AnnotatedString.Builder()
	resultBuilder.append(spannedString.toString())
	spannedString.getSpans<Annotation>(0, spannedString.length).forEach { annotation ->
		val spanStart = spannedString.getSpanStart(annotation)
		val spanEnd = spannedString.getSpanEnd(annotation)
		resultBuilder.addStringAnnotation(
			tag = annotation.key,
			annotation = annotation.value,
			start = spanStart,
			end = spanEnd
		)
		spanStyles(annotation)?.let { resultBuilder.addStyle(it, spanStart, spanEnd) }
	}
	return resultBuilder.toAnnotatedString()
}

fun Modifier.onFocusSelectAll(state: MutableState<TextFieldValue>): Modifier {
	return this.composed {
		var triggerEffect by remember { mutableStateOf<Boolean?>(value = null) }
		if (triggerEffect != null) {
			LaunchedEffect(triggerEffect) {
				val value = state.value
				state.value = value.copy(selection = TextRange(0, value.text.length))
			}
		}
		Modifier.onFocusChanged { focusState ->
			if (focusState.isFocused) {
				triggerEffect = triggerEffect?.not() ?: true
			}
		}
	}
}

@Composable
fun makeBulletedList(input: String): AnnotatedString {
	val textStyle = LocalTextStyle.current
	val textMeasurer = rememberTextMeasurer()
	val twoTabWidth = remember(textStyle, textMeasurer) {
		textMeasurer.measure(text = GAP, style = textStyle).size.width
	}
	val restLine = with(LocalDensity.current) { twoTabWidth.toSp() }
	val paragraphStyle = ParagraphStyle(textIndent = TextIndent(restLine = restLine))
	val spanStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)
	val list = input.split(NEWLINE)

	return buildAnnotatedString {
		list.forEach {
			withStyle(style = paragraphStyle) {
				append(BULLET)
				append(GAP)
				if (it.startsWith(Note.PREFIX_NOT_DONE)) {
					append(it.removePrefix(Note.PREFIX_NOT_DONE))
				}
				else {
					withStyle(style = spanStyle) {
						append(it.removePrefix(Note.PREFIX_DONE))
					}
				}
			}
		}
	}
}

private const val GAP = "\t\t"
private const val NEWLINE = "\n"
private const val BULLET = "\u2022"