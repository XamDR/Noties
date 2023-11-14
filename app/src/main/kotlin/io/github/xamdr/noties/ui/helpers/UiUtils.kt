package io.github.xamdr.noties.ui.helpers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import androidx.documentfile.provider.DocumentFile
import com.google.android.material.color.MaterialColors
import io.github.xamdr.noties.R

fun View.showSoftKeyboard() {
	if (this.requestFocus()) {
		val imm = this.context.getSystemService<InputMethodManager>()
		imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
	}
}

fun Context.copyUriToClipboard(@StringRes label: Int, uri: Uri, @StringRes copiedMsg: Int) {
	val manager = this.getSystemService<ClipboardManager>() ?: return
	val clip = ClipData.newUri(this.contentResolver, this.getString(label), uri)
	manager.setPrimaryClip(clip)
	this.showToast(copiedMsg)
}

val DocumentFile.simpleName: String?
	get() = this.name?.substringBeforeLast('.')

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

fun Window.setStatusBarColor(@ColorInt color: Int?) {
	if (color == null) {
		val defaultColor = MaterialColors.getColor(this.context, R.attr.colorSurface, String.Empty)
		this.statusBarColor = defaultColor
	}
	else {
		this.statusBarColor = color
	}
}

fun TextView.estimateNumberChars(numLines: Int): Int {
	val charWidth = this.paint.measureText("a").toInt()
	val screenWidth = this.context.resources.displayMetrics.widthPixels
	val numCharsPerLine = screenWidth / charWidth
	return numCharsPerLine * numLines
}

fun View.onClick(action: (View) -> Unit) {
	this.setOnClickListener(action)
}
