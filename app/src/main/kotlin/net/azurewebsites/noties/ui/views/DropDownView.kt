package net.azurewebsites.noties.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSpinner

class DropDownView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = androidx.appcompat.R.attr.spinnerStyle) : AppCompatSpinner(context, attrs, defStyle) {

	fun hideDropDown() = super.onDetachedFromWindow()
}