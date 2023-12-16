package io.github.xamdr.noties.ui.settings

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

fun setNightMode(appTheme: Int) {
	when (appTheme) {
		0 -> {
			AppCompatDelegate.setDefaultNightMode(
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
				else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
			)
		}
		1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
	}
}

