package net.azurewebsites.noties.ui.editor

import android.graphics.Paint
import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter
import net.azurewebsites.noties.ui.views.LinedEditText

@BindingAdapter("navigationOnClick")
fun bindNavigationListener(toolbar: Toolbar, listener: View.OnClickListener) {
	toolbar.setNavigationOnClickListener(listener)
}

@BindingAdapter("android:autoLink")
fun bindAutolink(editText: EditText, isAutolinkEnabled: Boolean) {
	editText.autoLinkMask = if (isAutolinkEnabled) Linkify.WEB_URLS else 0
}

@BindingAdapter("android:textSize")
fun bindTextSize(editText: EditText, size: Float) {
	editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
}

enum class EditorStyle {
	Blank,
	Striped;
}

@BindingAdapter("hasGridLines")
fun bindHasGridLines(editText: LinedEditText, value: String) {
	when (EditorStyle.valueOf(value)) {
		EditorStyle.Blank -> editText.hasGridLines = false
		EditorStyle.Striped -> editText.hasGridLines = true
	}
}

@BindingAdapter("strikethrough")
fun strikethrough(view: TextView, shouldStrike: Boolean) {
	view.paintFlags = if (shouldStrike) {
		view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
	}
	else {
		view.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
	}
}