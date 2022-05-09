package net.azurewebsites.noties.ui.helpers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.annotation.TransitionRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.core.os.ConfigurationCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.azurewebsites.noties.ui.MainActivity
import java.util.*

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

fun View.showSoftKeyboard() {
	if (this.requestFocus()) {
		val imm = context.getSystemService<InputMethodManager>()
		imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
	}
}

fun View.hideSoftKeyboard() {
	val imm = context.getSystemService<InputMethodManager>()
	imm?.hideSoftInputFromWindow(windowToken, 0)
}

fun Context.showToast(@StringRes text: Int, duration: Int = Toast.LENGTH_SHORT): Toast =
	Toast.makeText(this.applicationContext, text, duration).also { it.show() }

fun Context.copyTextToClipboard(@StringRes label: Int, text: CharSequence, @StringRes copiedMsg: Int) {
	val manager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
	manager.setPrimaryClip(ClipData.newPlainText(this.getString(label), text))
	this.showToast(copiedMsg)
}

fun Context.copyUriToClipboard(@StringRes label: Int, uri: Uri, @StringRes copiedMsg: Int) {
	val manager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
	val clip = ClipData.newUri(this.contentResolver, this.getString(label), uri)
	manager.setPrimaryClip(clip)
	this.showToast(copiedMsg)
}

fun <T : RecyclerView.ViewHolder> T.setOnClickListener(callback: (position: Int, type: Int) -> Unit): T {
	itemView.setOnClickListener {
		ViewCompat.postOnAnimationDelayed(it, {
			callback.invoke(bindingAdapterPosition, itemViewType)
		}, 100)
	}
	return this
}

fun RecyclerView.addItemTouchHelper(itemTouchHelper: ItemTouchHelper) {
	itemTouchHelper.attachToRecyclerView(this)
}

fun View.showSnackbar(@StringRes message: Int,
					  length: Int = Snackbar.LENGTH_LONG,
					  @StringRes action: Int,
					  listener: ((View) -> Unit)? = null): Snackbar {
	return Snackbar.make(this, message, length).setAction(action, listener).apply {
		behavior = BaseTransientBottomBar.Behavior().apply { setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY) }
	}.also { it.show() }
}

fun View.showSnackbar(@StringRes message: Int,
                      length: Int = Snackbar.LENGTH_LONG): Snackbar {
	return Snackbar.make(this, message, length).apply {
		behavior = BaseTransientBottomBar.Behavior().apply { setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY) }
	}.also { it.show() }
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
		printError(NavController::class.java.simpleName, e)
	}
}

val Fragment.mainActivity: MainActivity
	get() = requireActivity() as? MainActivity
		?: throw IllegalStateException("The activity this fragment is attached to does not extend MainActivity.")

fun Context.getCurrentLocale(): Locale {
	return ConfigurationCompat.getLocales(this.resources.configuration)[0]
}

fun Context.getUriMimeType(uri: Uri): String? = contentResolver.getType(uri)

fun Context.getUriExtension(uri: Uri): String? {
	val mimeType = contentResolver.getType(uri)
	return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
}

inline fun Fragment.launchAndRepeatWithViewLifecycle(
	minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
	crossinline block: suspend CoroutineScope.() -> Unit
) {
	viewLifecycleOwner.lifecycleScope.launch {
		viewLifecycleOwner.repeatOnLifecycle(minActiveState) {
			block()
		}
	}
}

inline fun DialogFragment.launchAndRepeatWithLifecycle(
	minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
	crossinline block: suspend CoroutineScope.() -> Unit
) {
	lifecycleScope.launch {
		repeatOnLifecycle(minActiveState) {
			block()
		}
	}
}

inline fun FragmentActivity.launchAndRepeatWithLifecycle(
	minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
	crossinline block: suspend CoroutineScope.() -> Unit
) {
	lifecycleScope.launch {
		repeatOnLifecycle(minActiveState) {
			block()
		}
	}
}

fun DialogFragment.getPositiveButton(): Button {
	return (requireDialog() as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
}