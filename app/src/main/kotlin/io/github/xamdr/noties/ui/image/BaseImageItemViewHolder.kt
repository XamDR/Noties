package io.github.xamdr.noties.ui.image

import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.ui.gallery.GalleryActivity
import io.github.xamdr.noties.ui.helpers.tryNavigate

open class BaseImageItemViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

	fun bind(image: Image) {
		binding.apply {
//			setVariable(io.xamdr.noties.BR.imageItem, image)
//			executePendingBindings()
		}
	}

	fun showMediaItemFullScreen(images: List<Image>, position: Int = 0) {
		BitmapCache.Instance.clear()
		val args = bundleOf(
			GalleryActivity.IMAGES to ArrayList(images),
			GalleryActivity.POSITION to position
		)
		itemView.findNavController().tryNavigate(R.id.action_editor_gallery, args)
	}
}