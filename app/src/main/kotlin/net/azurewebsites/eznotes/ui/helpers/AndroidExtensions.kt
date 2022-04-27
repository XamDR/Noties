package net.azurewebsites.eznotes.ui.helpers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.core.os.ConfigurationCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import net.azurewebsites.eznotes.ui.MainActivity
import java.util.*

fun Context.setNightMode() {
	val preferences = PreferenceManager.getDefaultSharedPreferences(this)
	when (preferences.getString("app_theme", "-1")?.toInt()) {
		0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
		-1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
	}
}

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

fun NavController.safeNavigate(
	@IdRes resId: Int,
	args: Bundle? = null,
	navOptions: NavOptions? = null,
	navigatorExtras: Navigator.Extras? = null
) {
	// We make use of a try-catch block to prevent an exception if the call to NavController.navigate()
	// happens too quickly. We shouldn't need to do this, but again the Android Team just didn't care
	// about this bug, apparently.
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