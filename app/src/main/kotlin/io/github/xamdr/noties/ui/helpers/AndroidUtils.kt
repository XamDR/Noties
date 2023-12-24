package io.github.xamdr.noties.ui.helpers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.annotation.TransitionRes
import androidx.core.content.getSystemService
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

fun FragmentActivity.findNavController(@IdRes id: Int) =
	(this.supportFragmentManager.findFragmentById(id) as NavHostFragment).navController

fun Fragment.inflateTransition(@TransitionRes resId: Int): Transition =
	TransitionInflater.from(this.requireContext()).inflateTransition(resId)

fun Context.showToast(@StringRes text: Int, duration: Int = Toast.LENGTH_SHORT): Toast =
	Toast.makeText(this.applicationContext, text, duration).also { it.show() }

@Suppress("DEPRECATION")
fun <T : Parcelable> Bundle.getParcelableCompat(key: String, clazz: Class<T>): T {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		(this.getParcelable(key, clazz) ?: clazz.newInstance()) as T
	}
	else {
		(this.getParcelable(key) ?: clazz.newInstance()) as T
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

@Suppress("DEPRECATION")
fun <T : Parcelable> Intent.getParcelableExtraCompat(key: String, clazz: Class<T>): T {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		(this.getParcelableExtra(key, clazz) ?: clazz.newInstance()) as T
	}
	else {
		return (this.getParcelableExtra(key) ?: clazz.newInstance()) as T
	}
}

fun <T : Parcelable> Intent.getParcelableArrayListCompat(key: String, clazz: Class<T>): ArrayList<T> {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		(this.getParcelableArrayListExtra(key, clazz) ?: emptyList<T>()) as ArrayList<T>
	}
	else {
		@Suppress("DEPRECATION")
		return (this.getParcelableArrayListExtra(key) ?: emptyList<T>()) as ArrayList<T>
	}
}

fun FragmentActivity.launch(block: suspend CoroutineScope.() -> Unit): Job {
	return this.lifecycleScope.launch(block = block)
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

fun Fragment.onBackPressed() {
	this.requireActivity().onBackPressedDispatcher.onBackPressed()
}

fun NavController.tryNavigate(
	@IdRes resId: Int,
	args: Bundle? = null,
	navOptions: NavOptions? = null,
	navigatorExtras: Navigator.Extras? = null
) {
	// We make use of a try-catch block to prevent an exception if the call to NavController.navigate()
	// happens too fast, e.g. the user clicks twice a button quickly.
	try {
		this.navigate(resId, args, navOptions, navigatorExtras)
	}
	catch (e: IllegalArgumentException) {
		Timber.e(e)
	}
}

fun Context.isLandscape(): Boolean {
	return this.resources.displayMetrics.heightPixels < this.resources.displayMetrics.widthPixels
}

fun <T> Fragment.getNavigationResult(key: String): T? {
	return this.findNavController().currentBackStackEntry?.savedStateHandle?.get(key)
}

fun <T> Fragment.setNavigationResult(key: String, value: T) {
	this.findNavController().previousBackStackEntry?.savedStateHandle?.set(key, value)
}

tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
	is ComponentActivity -> this
	else -> (this as? ContextWrapper)?.baseContext?.findActivity()
}

fun Context.copyUriToClipboard(@StringRes label: Int, uri: Uri, @StringRes copiedMsg: Int) {
	val manager = this.getSystemService<ClipboardManager>() ?: return
	val clip = ClipData.newUri(this.contentResolver, this.getString(label), uri)
	manager.setPrimaryClip(clip)
	this.showToast(copiedMsg)
}

fun Context.copyTextToClipboard(@StringRes label: Int, text: String, @StringRes copiedMsg: Int) {
	val manager = this.getSystemService<ClipboardManager>() ?: return
	val clip = ClipData.newPlainText(this.getString(label), text)
	manager.setPrimaryClip(clip)
	this.showToast(copiedMsg)
}

val DocumentFile.simpleName: String?
	get() = this.name?.substringBeforeLast('.')

fun extractUrls(input: String): List<String> = Regex(Patterns.WEB_URL.pattern())
	.findAll(input)
	.map { it.value }
	.toList()