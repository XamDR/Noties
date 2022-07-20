package net.azurewebsites.noties.ui.helpers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.core.content.getSystemService
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
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

fun <T : RecyclerView.ViewHolder> T.setOnClickListener(callback: (position: Int) -> Unit): T {
	itemView.setOnClickListener {
		ViewCompat.postOnAnimationDelayed(it, {
			callback.invoke(bindingAdapterPosition)
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

fun View.showSnackbar(message: String,
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

val Fragment.supportActionBar: ActionBar?
	get() = (this.activity as AppCompatActivity).supportActionBar

fun Context.getUriMimeType(uri: Uri): String? = contentResolver.getType(uri)

fun Context.getUriExtension(uri: Uri): String? {
	val mimeType = contentResolver.getType(uri)
	return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
}

fun DialogFragment.getPositiveButton(): Button {
	return (requireDialog() as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
}

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun View.setOnSingleClickListener(thresholdInMillis: Long = 500, action: (View) -> Unit) {
	this.setOnClickListener(ThrottledOnClickListener(thresholdInMillis, action))
}

fun Fragment.addMenuProvider(provider: MenuProvider, owner: LifecycleOwner) {
	this.requireActivity().addMenuProvider(provider, owner)
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

fun Fragment.startActionMode(callback: ActionMode.Callback): ActionMode? {
	return (this.requireActivity() as AppCompatActivity).startSupportActionMode(callback)
}

fun TextView.blur() {
	this.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
	val blurMask = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
	this.paint.maskFilter = blurMask
}

fun Context.copyUriToClipboard(@StringRes label: Int, uri: Uri, @StringRes copiedMsg: Int) {
	val manager = this.getSystemService<ClipboardManager>() ?: return
	val clip = ClipData.newUri(this.contentResolver, this.getString(label), uri)
	manager.setPrimaryClip(clip)
	this.showToast(copiedMsg)
}

val DocumentFile.simpleName: String?
	get() = this.name?.substringBeforeLast('.')

fun Context.getIntArray(@ArrayRes resId: Int) = this.resources.getIntArray(resId)

fun Toolbar.findItem(@IdRes resId: Int): MenuItem {
	return this.menu.findItem(resId)
}

fun TextView.strikethrough(shouldStrike: Boolean) {
	paintFlags = if (shouldStrike) {
		paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
	}
	else {
		paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
	}
}

fun Int.toColorInt(): Int {
	val hex = Integer.toHexString(this)
	return Color.parseColor("#$hex")
}