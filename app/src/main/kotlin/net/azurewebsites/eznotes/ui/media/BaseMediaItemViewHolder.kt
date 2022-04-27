package net.azurewebsites.eznotes.ui.media

import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.eznotes.BR
import net.azurewebsites.eznotes.R
import net.azurewebsites.eznotes.core.MediaItemEntity
import net.azurewebsites.eznotes.ui.helpers.safeNavigate
import net.azurewebsites.eznotes.ui.image.BitmapCache

open class BaseMediaItemViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

	fun bind(mediaItem: MediaItemEntity) {
		binding.apply {
			setVariable(BR.mediaItem, mediaItem)
			executePendingBindings()
		}
	}

	fun showMediaItemFullScreen(mediaItems: List<MediaItemEntity>, position: Int = 0) {
		BitmapCache.Instance.clear()
		val args = bundleOf(
			"items" to ArrayList(mediaItems),
			"pos" to position
		)
		itemView.findNavController().safeNavigate(R.id.action_editor_gallery, args)
	}
}