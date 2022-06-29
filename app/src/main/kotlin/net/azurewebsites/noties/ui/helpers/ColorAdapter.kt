package net.azurewebsites.noties.ui.helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.R

class ColorAdapter(private val colors: List<Int>) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

	inner class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val imageView: ImageView

		init {
			imageView = view.findViewById(R.id.color)
		}

		fun bind(color: Int) {
			imageView.setBackgroundColor(color)
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.color_item, parent, false)
		return ColorViewHolder(view).apply {
			setOnClickListener { position -> selectColor(position) }
		}
	}

	override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
		val color = colors[position]
		holder.bind(color)
	}

	override fun getItemCount() = colors.size

	private fun selectColor(position: Int) {

	}
}