package io.github.xamdr.noties.ui.notes

import androidx.cardview.widget.CardView
import com.google.android.material.color.MaterialColors
import io.github.xamdr.noties.R
import io.github.xamdr.noties.ui.helpers.toColorInt

fun bindBackgroundColor(cardView: CardView, color: Int?) {
	if (color != null) {
		cardView.setCardBackgroundColor(color.toColorInt())
	}
	else {
		val defaultColor = MaterialColors.getColor(cardView, R.attr.colorSurface)
		cardView.setCardBackgroundColor(defaultColor)
	}
}