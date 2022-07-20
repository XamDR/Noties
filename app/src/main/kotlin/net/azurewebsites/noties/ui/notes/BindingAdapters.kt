package net.azurewebsites.noties.ui.notes

import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.google.android.material.color.MaterialColors
import net.azurewebsites.noties.R
import net.azurewebsites.noties.ui.helpers.toColorInt

@BindingAdapter("backgroundColor")
fun bindBackgroundColor(cardView: CardView, color: Int?) {
	if (color != null) {
		cardView.setCardBackgroundColor(color.toColorInt())
	}
	else {
		val defaultColor = MaterialColors.getColor(cardView, R.attr.colorSurface)
		cardView.setCardBackgroundColor(defaultColor)
	}
}