package net.azurewebsites.noties.ui.image

import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.BR
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.ui.gallery.GalleryActivity
import net.azurewebsites.noties.ui.helpers.tryNavigate

open class BaseImageItemViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

	fun bind(image: ImageEntity) {
		binding.apply {
			setVariable(BR.imageItem, image)
			executePendingBindings()
		}
	}

	fun showMediaItemFullScreen(images: List<ImageEntity>, position: Int = 0) {
		BitmapCache.Instance.clear()
		val args = bundleOf(
			GalleryActivity.IMAGES to ArrayList(images),
			GalleryActivity.POSITION to position
		)
		itemView.findNavController().tryNavigate(R.id.action_editor_gallery, args)
	}
}