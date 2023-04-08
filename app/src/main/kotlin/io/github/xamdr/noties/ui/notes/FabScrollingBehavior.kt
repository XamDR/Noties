package io.github.xamdr.noties.ui.notes

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

// Based on these answers:
// https://stackoverflow.com/a/46245780/8781554
// https://stackoverflow.com/a/64807240/8781554
class FabScrollingBehavior : FloatingActionButton.Behavior() {

	// Changes visibility from GONE to INVISIBLE when fab is hidden because
	// due to CoordinatorLayout.onStartNestedScroll() implementation
	// child view's (here, fab) onStartNestedScroll won't be called anymore
	// because it's visibility is GONE
	private val listener = object : FloatingActionButton.OnVisibilityChangedListener() {
		override fun onHidden(fab: FloatingActionButton?) {
			fab?.visibility = View.INVISIBLE
		}
	}

	override fun onNestedPreScroll(
		layout: CoordinatorLayout,
		fab: FloatingActionButton,
		target: View,
		dx: Int,
		dy: Int,
		consumed: IntArray,
		type: Int
	) {
		super.onNestedPreScroll(layout, fab, target, dx, dy, consumed, type)
		if (dy > 0 && fab.visibility == View.VISIBLE) {
			fab.hide(listener)
		}
		else if (dy < 0 && fab.visibility == View.INVISIBLE) {
			fab.show()
		}
	}

	override fun onStartNestedScroll(
		layout: CoordinatorLayout,
		fab: FloatingActionButton,
		directTargetChild: View,
		target: View,
		axes: Int,
		type: Int) = axes == ViewCompat.SCROLL_AXIS_VERTICAL
}