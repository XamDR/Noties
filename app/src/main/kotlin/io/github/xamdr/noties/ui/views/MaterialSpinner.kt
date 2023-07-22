package io.github.xamdr.noties.ui.views

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView

class MaterialSpinner<T> @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = com.google.android.material.R.attr.autoCompleteTextViewStyle) : AppCompatAutoCompleteTextView(context, attrs, defStyle) {

	var selectedItem: T? = null
	private var listener: OnItemSelectedListener? = null

	init {
		init()
	}

	fun setOnItemSelectedListener(listener: OnItemSelectedListener) {
		this.listener = listener
	}

	fun setSelectedValue(text: CharSequence) {
		setText(text, false)
	}

	@Suppress("UNCHECKED_CAST")
	private fun init() {
		inputType = InputType.TYPE_NULL
		setTextIsSelectable(false)
		setOnKeyListener { _, _, _ -> true }
		setOnItemClickListener { parent, view, position, id ->
			selectedItem = adapter.getItem(position) as T
			listener?.onItemClick(parent, view, position, id)
		}
	}

	interface OnItemSelectedListener {
		fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long)
	}
}