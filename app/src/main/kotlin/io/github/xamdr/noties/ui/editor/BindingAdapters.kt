package io.github.xamdr.noties.ui.editor

import android.graphics.Paint
import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.color.MaterialColors
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.toColorInt
import io.github.xamdr.noties.ui.views.LinedEditText

fun bindNavigationListener(toolbar: Toolbar, listener: View.OnClickListener) {
	toolbar.setNavigationOnClickListener(listener)
}

fun bindAutolink(editText: EditText, isAutolinkEnabled: Boolean) {
	editText.autoLinkMask = if (isAutolinkEnabled) Linkify.WEB_URLS else 0
}

fun bindTextSize(editText: EditText, size: Float) {
	editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
}

enum class EditorStyle {
	Blank,
	Striped;
}

fun bindHasGridLines(editText: LinedEditText, value: String) {
	when (EditorStyle.valueOf(value)) {
		EditorStyle.Blank -> editText.hasGridLines = false
		EditorStyle.Striped -> editText.hasGridLines = true
	}
}

fun strikethrough(view: TextView, shouldStrike: Boolean) {
	view.paintFlags = if (shouldStrike) {
		view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
	}
	else {
		view.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
	}
}

fun bindBackgroundCOlor(view: View, color: Int?) {
	if (color != null) {
		view.setBackgroundColor(color.toColorInt())
	}
	else {
		val defaultColor = MaterialColors.getColor(view, R.attr.colorSurface)
		view.setBackgroundColor(defaultColor)
	}
}