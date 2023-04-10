package io.github.xamdr.noties.ui.helpers

import android.os.Build
import android.os.Bundle
import java.io.Serializable

@Suppress("UNCHECKED_CAST")
fun <T : Serializable> Bundle.getSerializableCompat(key: String, clazz: Class<T>): T {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		(this.getSerializable(key, clazz) ?: clazz.newInstance()) as T
	}
	else {
		@Suppress("DEPRECATION")
		(this.getSerializable(key) ?: clazz.newInstance()) as T
	}
}