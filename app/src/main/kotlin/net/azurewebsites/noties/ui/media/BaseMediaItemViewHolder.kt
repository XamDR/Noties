package net.azurewebsites.noties.ui.media

import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
//import net.azurewebsites.noties.BR
import net.azurewebsites.noties.R
import net.azurewebsites.noties.domain.ImageEntity
import net.azurewebsites.noties.ui.helpers.tryNavigate
import net.azurewebsites.noties.ui.image.BitmapCache

open class BaseMediaItemViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

	fun bind(image: ImageEntity) {
		binding.apply {
//			setVariable(BR.mediaItem, image)
			executePendingBindings()
		}
	}

	fun showMediaItemFullScreen(images: List<ImageEntity>, position: Int = 0) {
		BitmapCache.Instance.clear()
		val args = bundleOf(
			"items" to ArrayList(images),
			"pos" to position
		)
		itemView.findNavController().tryNavigate(R.id.action_editor_gallery, args)
	}
}