package net.azurewebsites.eznotes.ui.notes.urls

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import net.azurewebsites.eznotes.databinding.UrlItemBinding
import net.azurewebsites.eznotes.ui.helpers.setOnClickListener

class UrlAdapter(private val urls: List<String>) : RecyclerView.Adapter<UrlAdapter.UrlViewHolder>() {

	class UrlViewHolder(private val binding: UrlItemBinding) : RecyclerView.ViewHolder(binding.root) {

		fun bind(url: String) {
			binding.apply {
				this.url = url
				executePendingBindings()
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlViewHolder {
		val binding = UrlItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return UrlViewHolder(binding).setOnClickListener { position, _ ->
			openUrl(position, parent.context)
		}
	}

	override fun onBindViewHolder(holder: UrlViewHolder, position: Int) {
		val url = urls[position]
		holder.bind(url)
	}

	override fun getItemCount() = urls.size

	override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
		super.onAttachedToRecyclerView(recyclerView)
		recyclerView.addItemDecoration(
			MaterialDividerItemDecoration(recyclerView.context, MaterialDividerItemDecoration.VERTICAL)
		)
	}

	fun setOnUrlOpenedListener(callback: () -> Unit) {
		onUrlOpenedCallback = callback
	}

	private fun openUrl(position: Int, context: Context) {
		onUrlOpenedCallback?.invoke()
		val url = urls[position]
		context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
	}

	private var onUrlOpenedCallback: (() -> Unit)? = null
}