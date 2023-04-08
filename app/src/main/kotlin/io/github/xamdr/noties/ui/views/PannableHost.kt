package io.github.xamdr.noties.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.sign

// This solution is based on the following code:
// https://github.com/android/views-widgets-samples/blob/master/ViewPager2/app/src/main/java/androidx/viewpager2/integration/testapp/NestedScrollableHost.kt

/**
 * Layout to wrap a [PhotoImageView] inside a ViewPager2.
 * Provided as a solution to the problem where the ViewPager2 intercepts touch events making difficult to pan a zoomed image in PhotoImageView.
 * @see <a href="https://github.com/android/views-widgets-samples/blob/master/ViewPager2/app/src/main/java/androidx/viewpager2/integration/testapp/NestedScrollableHost.kt">NestedScrollableHost.kt</a>
 */
class PannableHost : FrameLayout {
	constructor(context: Context) : super(context)
	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

	private var touchSlop = 0
	private val parentViewPager: ViewPager2?
		get() {
			var view: View? = parent as? View
			while (view != null && view !is ViewPager2) {
				view = view.parent as? View
			}
			return view as? ViewPager2
		}

	private val child: View? get() = if (childCount > 0) getChildAt(0) else null

	init {
		touchSlop = ViewConfiguration.get(context).scaledTouchSlop
	}

	private fun canChildScroll(orientation: Int, delta: Float): Boolean {
		val direction = -delta.sign.toInt()
		return when (orientation) {
			0 -> child?.canScrollHorizontally(direction) ?: false
			1 -> child?.canScrollVertically(direction) ?: false
			else -> throw IllegalArgumentException()
		}
	}

	override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
		handleInterceptTouchEvent()
		return super.onInterceptTouchEvent(e)
	}

	private fun handleInterceptTouchEvent() {
		val orientation = parentViewPager?.orientation ?: return

		// Early return if child can't scroll in same direction as parent
		if (!canChildScroll(orientation, -1f) && !canChildScroll(orientation, 1f)) {
			return
		}
		if (child is PhotoImageView) {
			if ((child as PhotoImageView).isZoomed) {
				parent.requestDisallowInterceptTouchEvent(true)
			}
			else {
				parent.requestDisallowInterceptTouchEvent(false)
			}
		}
	}
}