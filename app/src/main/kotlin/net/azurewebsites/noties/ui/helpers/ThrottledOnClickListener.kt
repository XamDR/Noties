package net.azurewebsites.noties.ui.helpers

import android.view.View

/**
 * A [View.OnClickListener] that throttles multiple clicks.
 * Useful for performing actions in response to rapid user input where you want to take action on
 * the initial input but prevent follow-up spam
 */
class ThrottledOnClickListener(private val thresholdInMillis: Long, val action: (View) -> Unit) : View.OnClickListener {

	override fun onClick(v: View) {
		if (!consumed) {
			consumed = true
			v.postDelayed(runnable, thresholdInMillis)
			action(v)
		}
	}

	private companion object {
		private var consumed = false
		private val runnable = Runnable { consumed = false }
	}
}