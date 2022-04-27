package net.azurewebsites.noties.ui.image

import android.graphics.Bitmap
import androidx.collection.LruCache

// Basic implementation of a in-memory cache for bitmaps (images).
// Based on these sources:
// https://developer.android.com/topic/performance/graphics/cache-bitmap
// https://stackoverflow.com/a/22855962/8781554
// https://github.com/capecrawler/BitmapFun/blob/master/BitmapFun/src/com/example/android/bitmapfun/util/ImageCache.java
class BitmapCache private constructor() {

	private lateinit var memoryCache: LruCache<String, Bitmap>
	private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

	fun addBitmapToMemoryCache(key: String, value: Bitmap) {
		if (getBitmapFromMemCache(key) == null) {
			memoryCache.put(key, value)
		}
	}

	fun getBitmapFromMemCache(key: String): Bitmap? = memoryCache.get(key)

	fun clear() = memoryCache.evictAll()

	private fun initMemoryCache() {
		memoryCache = object : LruCache<String, Bitmap>(maxMemory / 8) {
			override fun sizeOf(key: String, value: Bitmap): Int {
				val size = value.allocationByteCount / 1024
				return if (size == 0) 1 else size
			}
		}
	}

	companion object {
		val Instance: BitmapCache get() = instance ?: throw IllegalStateException("Cache not initialized.")
		private var instance: BitmapCache? = null

		fun initialize(): BitmapCache {
			if (instance == null) {
				instance = BitmapCache()
				instance?.initMemoryCache()
			}
			return instance as BitmapCache
		}
	}
}