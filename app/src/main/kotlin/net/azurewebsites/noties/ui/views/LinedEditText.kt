package net.azurewebsites.noties.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.Spannable
import android.text.TextUtils
import android.text.method.ArrowKeyMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.text.getSpans
import net.azurewebsites.noties.R
import kotlin.math.max

// Based on these answers:
// https://stackoverflow.com/a/6111460/8781554
// https://stackoverflow.com/a/6259234/8781554
/**
 * A custom EditText that draws lines so it looks like a notepad.
 */
class LinedEditText @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = androidx.appcompat.R.attr.editTextStyle) : AppCompatEditText(context, attrs, defStyle) {

	init {
		init(attrs, defStyle)
		movementMethod = ArrowKeyLinkMovementMethod.getInstance()
	}

	private val bottomPadding = 1
	private val rect = Rect()
	private val paint = Paint().apply {
		style = Paint.Style.STROKE
		color = Color.parseColor("#FFAAAAAA") // @android:color/darker_gray
	}
	
	private var _hasGridLines = false
	var hasGridLines: Boolean
		get() = _hasGridLines
		set(value) {
			_hasGridLines = value
			invalidate()
		}

	private fun init(attrs: AttributeSet?, defStyle: Int) {
		val a = context.obtainStyledAttributes(attrs, R.styleable.LinedEditText, defStyle, 0)
		_hasGridLines = a.getBoolean(R.styleable.LinedEditText_hasGridLines, false)
		a.recycle()
	}

	override fun onDraw(canvas: Canvas) {
		if (hasGridLines) {
			val lineHeight = lineHeight
			val numberOfLines = max(lineCount, height / lineHeight) // for long text with scrolling
			var baseline = getLineBounds(0, rect) // first line

			for (i in 0 until numberOfLines) {
				canvas.drawLine(rect.left.toFloat(), (baseline + bottomPadding).toFloat(), rect.right.toFloat(),
					(baseline + bottomPadding).toFloat(), paint)
				baseline += lineHeight // next line
			}
		}
		super.onDraw(canvas)
	}

	override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
		if (keyCode == KeyEvent.KEYCODE_BACK && TextUtils.isEmpty(text)) {
			emptyContentCallback.invoke()
			return true
		}
		return super.onKeyPreIme(keyCode, event)
	}

	// Disable spell checking, but keep IME suggestions.
	override fun isSuggestionsEnabled() = false

	private var emptyContentCallback: () -> Unit = {}

	private var callback: (linkText: String) -> Unit = {}

	fun setOnLinkClickedListener(callback: (linkText: String) -> Unit) {
		this.callback = callback
	}

	class ArrowKeyLinkMovementMethod private constructor() : ArrowKeyMovementMethod() {

		override fun onTouchEvent(textView: TextView, buffer: Spannable, event: MotionEvent?): Boolean {
			val action = event?.action

			if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
				val x = event.x - textView.totalPaddingLeft
				val y = event.y - textView.totalPaddingTop
				val scrollX = x + textView.scrollX
				val scrollY = y + textView.scrollY
				val layout = textView.layout
				val line = layout.getLineForVertical(scrollY.toInt())
				val offsetForHorizontal = layout.getOffsetForHorizontal(line, scrollX)
				val links = buffer.getSpans<ClickableSpan>(offsetForHorizontal, offsetForHorizontal)

				if (links.isNotEmpty()) {
					if (offsetForHorizontal >= buffer.getSpanEnd(links[0]) ||
						offsetForHorizontal <= buffer.getSpanStart(links[0])) {
						return super.onTouchEvent(textView, buffer, event)
					}
					if (action == MotionEvent.ACTION_UP) {
						val start = buffer.getSpanStart(links[0])
						val end = buffer.getSpanEnd(links[0])
						val url = buffer.toString().substring(start, end)
						(textView as LinedEditText).callback.invoke(url)
						return true
					}
				}
			}
			return super.onTouchEvent(textView, buffer, event)
		}

		companion object {

			fun getInstance(): ArrowKeyLinkMovementMethod {
				if (instance == null) {
					instance = ArrowKeyLinkMovementMethod()
				}
				return instance ?: throw IllegalStateException("Not initialized.")
			}

			private var instance: ArrowKeyLinkMovementMethod? = null
		}
	}
}
