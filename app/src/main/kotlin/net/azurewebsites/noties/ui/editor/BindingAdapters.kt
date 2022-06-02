package net.azurewebsites.noties.ui.editor

import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.databinding.BindingAdapter
import net.azurewebsites.noties.ui.views.LinedEditText
import net.azurewebsites.noties.util.EditorStyle

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

@BindingAdapter("hasGridLines")
fun bindHasGridLines(editText: LinedEditText, value: String) {
	when (EditorStyle.from(value)) {
		EditorStyle.Blank -> editText.hasGridLines = false
		EditorStyle.Striped -> editText.hasGridLines = true
		else -> throw Exception("Unknown editor style.")
	}
}