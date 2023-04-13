package io.github.xamdr.noties

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.xamdr.noties.ui.image.BitmapCache
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

	override fun onCreate() {
		super.onCreate()
		BitmapCache.initialize()
		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
		}
	}
}