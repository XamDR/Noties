package io.github.xamdr.noties.ui.helpers

import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import kotlin.math.max
import kotlin.math.min

class Debouncer(private val delay: Long = INITIAL_DELAY, private val action: () -> Unit) {

	private val handler = Handler(Looper.getMainLooper())
	private lateinit var lastRunnable: Runnable
	private var lastEventTime = 0L
	private var lastDelay = 0L

	fun debounce() {
		val currentTime = System.currentTimeMillis()
		val elapsedTime = currentTime - lastEventTime
		var newDelay = delay
		// The event happens too fast, so we need to decrease the delay
		if (elapsedTime < delay && lastDelay > MIN_DELAY) {
			newDelay = max(newDelay - lastDelay, MIN_DELAY)
		}
		// The event happens too slow, so we need to increase the delay
		else if (elapsedTime > delay && lastDelay < MAX_DELAY) {
			newDelay = min(lastDelay + elapsedTime - delay, MAX_DELAY)
		}
		lastEventTime = currentTime
		lastDelay = newDelay

		if (::lastRunnable.isInitialized) {
			handler.removeCallbacks(lastRunnable)
		}
		lastRunnable = handler.postDelayed(newDelay) { action() }
	}

	companion object {
		private const val INITIAL_DELAY = 1000L
		private const val MAX_DELAY = 2000L
		private const val MIN_DELAY = 500L
	}
}