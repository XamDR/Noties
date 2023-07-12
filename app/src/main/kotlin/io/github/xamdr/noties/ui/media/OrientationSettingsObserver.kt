package io.github.xamdr.noties.ui.media

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Handler
import android.provider.Settings
import androidx.fragment.app.FragmentActivity

class OrientationSettingsObserver(handler: Handler, private val activity: FragmentActivity) : ContentObserver(handler) {

	override fun onChange(selfChange: Boolean) {
		super.onChange(selfChange)
		if (!isGlobalScreenOrientationLocked(activity)) {
			activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
		}
	}

	private fun isGlobalScreenOrientationLocked(context: Context): Boolean {
		// 1: Screen orientation changes using accelerometer
		// 0: Screen orientation is locked
		// if the accelerometer sensor is missing completely, assume locked orientation
		return Settings.System.getInt(
			context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 0 ||
				!context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)
	}
}