package net.azurewebsites.eznotes

import android.app.Application
import net.azurewebsites.eznotes.data.AppRepository
import net.azurewebsites.eznotes.ui.image.BitmapCache

@Suppress("unused")
class App : Application() {

	override fun onCreate() {
		super.onCreate()
		AppRepository.initialize(this)
		BitmapCache.initialize()
	}
}