package net.azurewebsites.noties.ui.helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.R

class ColorAdapter(private val colors: List<Int>) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

	var selectedPosition = 0

	inner class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		private val imageView: ImageView

		init {
			imageView = view.findViewById(R.id.color)
		}

		fun bind(color: Int) {
			imageView.setBackgroundColor(color)
		}

		fun setImageResource(@DrawableRes resId: Int) {
			imageView.setImageResource(resId)
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.color_item, parent, false)
		return ColorViewHolder(view).apply {
			setOnClickListener { position -> onColorSelectedCallback(position) }
		}
	}

	override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
		val color = colors[position]
		holder.bind(color)
		if (selectedPosition == position) {
			holder.setImageResource(R.drawable.ic_check)
		}
	}

	override fun getItemCount() = colors.size

	fun setOnColorSelectedListener(callback: (position: Int) -> Unit) {
		onColorSelectedCallback = callback
	}

	private var onColorSelectedCallback: (position: Int) -> Unit = {}
}