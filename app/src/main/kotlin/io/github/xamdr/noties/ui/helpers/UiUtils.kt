package io.github.xamdr.noties.ui.helpers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.github.xamdr.noties.R
import kotlinx.coroutines.*

fun View.showSoftKeyboard() {
	if (this.requestFocus()) {
		val imm = this.context.getSystemService<InputMethodManager>()
		imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
	}
}

fun View.hideSoftKeyboard() {
	val imm = context.getSystemService<InputMethodManager>()
	imm?.hideSoftInputFromWindow(windowToken, 0)
}

fun <T : RecyclerView.ViewHolder> T.setOnClickListener(callback: (view: View?, position: Int) -> Unit): T {
	itemView.setOnClickListener {
		ViewCompat.postOnAnimationDelayed(it, {
			callback.invoke(it, bindingAdapterPosition)
		}, 100)
	}
	return this
}

fun RecyclerView.addItemTouchHelper(itemTouchHelper: ItemTouchHelper) {
	itemTouchHelper.attachToRecyclerView(this)
}

fun View.showSnackbar(
	@StringRes message: Int,
	length: Int = Snackbar.LENGTH_LONG
): Snackbar {
	return Snackbar.make(this, message, length).apply {
		behavior = BaseTransientBottomBar.Behavior().apply { setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY) }
	}.also { it.show() }
}

fun View.showSnackbarWithAction(
	message: String,
	length: Int = Snackbar.LENGTH_LONG,
	@StringRes actionText: Int,
	action: (View) -> Unit
): Snackbar {
	return Snackbar.make(this, message, length).setAction(actionText, action).apply {
		behavior = BaseTransientBottomBar.Behavior().apply { setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY) }
	}.also { it.show() }
}

fun View.showSnackbarWithAction(
	@StringRes message: Int,
	length: Int = Snackbar.LENGTH_LONG,
	@StringRes actionText: Int,
	action: (View) -> Unit
): Snackbar = this.showSnackbarWithAction(this.context.getString(message), length, actionText, action)

fun View.showSnackbarWithActionSuspend(
	@StringRes message: Int,
	length: Int = Snackbar.LENGTH_LONG,
	@StringRes actionText: Int,
	action: suspend () -> Unit
): Snackbar {
	var actionJob: Job? = null
	return Snackbar.make(this, message, length).setAction(actionText) {
		actionJob = CoroutineScope(Dispatchers.Main).launch {
			withContext(Dispatchers.Default) { action() }
		}
	}.apply {
		behavior = BaseTransientBottomBar.Behavior().apply { setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY) }
		addCallback(object : Snackbar.Callback() {
			override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
				super.onDismissed(transientBottomBar, event)
				actionJob?.cancel()
			}
		})
	}.also { it.show() }
}

fun View.setOnSingleClickListener(thresholdInMillis: Long = 500, action: (View) -> Unit) {
	this.setOnClickListener(ThrottledOnClickListener(thresholdInMillis, action))
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

/**
 * Sets the background color for this view or uses the colorSurface value
 * if the color provided is null.
 */
fun View.setBackgroundColor(@ColorInt color: Int?) {
	if (color == null) {
		val defaultColor = MaterialColors.getColor(this, R.attr.colorSurface)
		this.setBackgroundColor(defaultColor)
	}
	else {
		this.setBackgroundColor(color)
	}
}

fun Window.setStatusBarColor(@ColorInt color: Int?) {
	if (color == null) {
		val defaultColor = MaterialColors.getColor(this.context, R.attr.colorSurface, String.Empty)
		this.statusBarColor = defaultColor
	}
	else {
		this.statusBarColor = color
	}
}

val EditText.textString: String
	get() = if (this.text != null) this.text.toString() else String.Empty

fun Snackbar.showOnTop() {
	val typedValue = TypedValue()
	var toolbarHeight = 0
	if (this.context.theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
		toolbarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, this.context.resources.displayMetrics)
	}
	this.view.layoutParams = (this.view.layoutParams as FrameLayout.LayoutParams).apply {
		gravity = Gravity.TOP
		setMargins(this.leftMargin * 2, (toolbarHeight * 1.5).toInt(), this.rightMargin * 2, this.bottomMargin)
	}
}