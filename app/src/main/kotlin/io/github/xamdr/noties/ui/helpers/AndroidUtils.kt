package io.github.xamdr.noties.ui.helpers

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultRegistry
import androidx.annotation.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.MenuProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

fun FragmentActivity.findNavController(@IdRes id: Int) =
	(this.supportFragmentManager.findFragmentById(id) as NavHostFragment).navController

fun Context.setNightMode() {
	val preferences = PreferenceManager.getDefaultSharedPreferences(this)
	when (preferences.getString("app_theme", "-1")?.toInt()) {
		0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
		-1 -> AppCompatDelegate.setDefaultNightMode(
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
			else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
		)
	}
}

fun Context.getThemeColor(@AttrRes colorAttributeResId: Int) =
	MaterialColors.getColor(this, colorAttributeResId, Color.TRANSPARENT)

fun Fragment.inflateTransition(@TransitionRes resId: Int): Transition =
	TransitionInflater.from(this.requireContext()).inflateTransition(resId)

fun Context.showToast(@StringRes text: Int, duration: Int = Toast.LENGTH_SHORT): Toast =
	Toast.makeText(this.applicationContext, text, duration).also { it.show() }

fun Context.showToast(text: String, duration: Int = Toast.LENGTH_SHORT): Toast =
	Toast.makeText(this.applicationContext, text, duration).also { it.show() }

fun Context.getIntArray(@ArrayRes resId: Int) = this.resources.getIntArray(resId)

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

fun Fragment.launch(block: suspend CoroutineScope.() -> Unit): Job {
	return this.viewLifecycleOwner.lifecycleScope.launch(block = block)
}

fun DialogFragment.launch(block: suspend CoroutineScope.() -> Unit): Job {
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

val Fragment.supportActionBar: ActionBar?
	get() = (this.activity as AppCompatActivity).supportActionBar

val Fragment.activityResultRegistry: ActivityResultRegistry
	get() = this.requireActivity().activityResultRegistry

fun Fragment.addMenuProvider(provider: MenuProvider, owner: LifecycleOwner) {
	this.requireActivity().addMenuProvider(provider, owner)
}

fun Fragment.addMenuProvider(provider: MenuProvider) {
	this.requireActivity().addMenuProvider(provider)
}

fun Fragment.removeMenuProvider(provider: MenuProvider) {
	this.requireActivity().removeMenuProvider(provider)
}

fun Fragment.showDialog(dialog: DialogFragment, tag: String) {
	val previousDialog = this.childFragmentManager.findFragmentByTag(tag)
	if (previousDialog == null) {
		dialog.show(childFragmentManager, tag)
	}
}

val Fragment.window: Window
	get() = requireActivity().window

fun ActionBar.show(title: String) {
	this.apply {
		show()
		setTitle(title)
	}
}

fun DialogFragment.getPositiveButton(): Button {
	return (requireDialog() as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
}

fun Context.isLandscape(): Boolean {
	return this.resources.displayMetrics.heightPixels < this.resources.displayMetrics.widthPixels
}
