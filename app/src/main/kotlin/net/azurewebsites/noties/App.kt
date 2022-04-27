package net.azurewebsites.noties

import android.app.Application
import net.azurewebsites.noties.data.AppRepository
import net.azurewebsites.noties.ui.image.BitmapCache

@Suppress("unused")
class App : Application() {

	override fun onCreate() {
		super.onCreate()
		AppRepository.initialize(this)
		BitmapCache.initialize()
	}
}