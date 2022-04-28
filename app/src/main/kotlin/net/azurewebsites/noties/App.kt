@file:Suppress("unused")

package net.azurewebsites.noties

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.azurewebsites.noties.data.AppRepository
import net.azurewebsites.noties.ui.image.BitmapCache

@HiltAndroidApp
class App : Application() {

	override fun onCreate() {
		super.onCreate()
		AppRepository.initialize(this)
		BitmapCache.initialize()
	}
}