package net.azurewebsites.noties

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Gets the value of a [LiveData] or waits for it to have one, with a timeout.
 *
 * Use this extension from host-side (JVM) tests. It's recommended to use it alongside
 * `InstantTaskExecutorRule` or a similar mechanism to execute tasks synchronously.
 */
fun <T> LiveData<T>.getValueForTesting(): T? {
	var data: T? = null
	val latch = CountDownLatch(1)
	val observer = object : Observer<T> {
		override fun onChanged(o: T?) {
			data = o
			latch.countDown()
			this@getValueForTesting.removeObserver(this)
		}
	}
	this.observeForever(observer)

	// Don't wait indefinitely if the LiveData is not set.
	if (!latch.await(2, TimeUnit.SECONDS)) {
		this.removeObserver(observer)
		throw TimeoutException("LiveData value was never set.")
	}
	return data
}

///**
// * Observes a [LiveData] until the `block` is done executing.
// */
//fun <T> LiveData<T>.observeForTesting(block: () -> Unit) {
//	val observer = Observer<T> { }
//	try {
//		observeForever(observer)
//		block()
//	} finally {
//		removeObserver(observer)
//	}
//}