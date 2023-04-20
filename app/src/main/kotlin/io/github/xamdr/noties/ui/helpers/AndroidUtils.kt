package io.github.xamdr.noties.ui.helpers

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.widget.AppCompatEditText
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

@Suppress("UNCHECKED_CAST")
fun <T : Serializable> Intent.getSerializableListExtraCompat(key: String, clazz: Class<T>): List<T> {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		(this.getSerializableExtra(key, clazz) ?: emptyList<T>()) as List<T>
	}
	else {
		@Suppress("DEPRECATION")
		(this.getSerializableExtra(key) ?: emptyList<T>()) as List<T>
	}
}

fun AppCompatEditText.textAsString(): String {
	return if (this.text != null) this.text.toString() else String.Empty
}