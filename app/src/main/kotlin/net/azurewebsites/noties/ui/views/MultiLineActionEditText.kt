package net.azurewebsites.noties.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText

// Based on this answer: https://stackoverflow.com/a/12570003/8781554
/**
 * A multi-line EditText that supports IME actions.
 */
class MultiLineActionEditText @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = androidx.appcompat.R.attr.editTextStyle) : AppCompatEditText(context, attrs, defStyle) {

	override fun isSuggestionsEnabled() = false

	override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
		val inputConnection = super.onCreateInputConnection(outAttrs)
		outAttrs.imeOptions = outAttrs.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION.inv()
		return inputConnection
	}
}