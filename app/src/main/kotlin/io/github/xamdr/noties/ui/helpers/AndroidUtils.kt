package io.github.xamdr.noties.ui.helpers

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.ArrayList

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
fun <T : Parcelable> Bundle.getParcelableCompat(key: String, clazz: Class<T>): T {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		(this.getParcelable(key, clazz) ?: clazz.newInstance()) as T
	}
	else {
		@Suppress("DEPRECATION")
		(this.getParcelable(key) ?: clazz.newInstance()) as T
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

fun <T : Parcelable> Bundle.getParcelableArrayListCompat(key: String, clazz: Class<T>): ArrayList<T> {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		(this.getParcelableArrayList(key, clazz) ?: emptyList<T>()) as ArrayList<T>
	}
	else {
		@Suppress("DEPRECATION")
		return (this.getParcelableArrayList(key) ?: emptyList<T>()) as ArrayList<T>
	}
}

fun Fragment.launch(block: suspend CoroutineScope.() -> Unit): Job {
	return this.viewLifecycleOwner.lifecycleScope.launch(block = block)
}

fun Fragment.onBackPressed(block: () -> Unit) {
	this.requireActivity().onBackPressedDispatcher.addCallback(
		this.viewLifecycleOwner,
		object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				block()
			}
		}
	)
}

@Suppress("UNCHECKED_CAST")
fun <T : FragmentActivity> Fragment.getParentActivity() = this.requireActivity() as T