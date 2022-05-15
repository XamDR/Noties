package net.azurewebsites.noties.ui.helpers

import android.os.Handler
import android.os.Looper
import android.view.View

/**
 * A [View.OnClickListener] that throttles multiple clicks.
 * Useful for performing actions in response to rapid user input where you want to take action on
 * the initial input but prevent follow-up spam.
 * Based on [DebouncingOnClickListener](https://github.com/JakeWharton/butterknife/blob/master/butterknife-runtime/src/main/java/butterknife/internal/DebouncingOnClickListener.java)
 */
class ThrottledOnClickListener(private val thresholdInMillis: Long, val action: (View) -> Unit) : View.OnClickListener {

	override fun onClick(v: View) {
		if (!consumed) {
			consumed = true
			main.postDelayed(runnable, thresholdInMillis)
			action(v)
		}
	}

	private companion object {
		private var consumed = false
		private val runnable = Runnable { consumed = false }
		private val main = Handler(Looper.getMainLooper())
	}
}