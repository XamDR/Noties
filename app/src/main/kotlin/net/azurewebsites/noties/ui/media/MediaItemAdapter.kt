package net.azurewebsites.noties.ui.media

import android.annotation.SuppressLint
import android.view.*
import android.widget.EditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.azurewebsites.noties.R
import net.azurewebsites.noties.domain.ImageEntity
import net.azurewebsites.noties.databinding.ImageItemBinding
import net.azurewebsites.noties.databinding.SingleImageItemBinding
import net.azurewebsites.noties.ui.helpers.printDebug
import net.azurewebsites.noties.ui.helpers.setOnClickListener
import net.azurewebsites.noties.util.isSingleton

class MediaItemAdapter : ListAdapter<ImageEntity, BaseMediaItemViewHolder>(MediaItemAdapterCallback()) {

	inner class ImageMediaItemViewHolder(binding: ImageItemBinding) : BaseMediaItemViewHolder(binding) {
		init {
			binding.image.setOnCreateContextMenuListener { menu, view, _ ->
				showContextMenu(menu, view, bindingAdapterPosition)
			}
		}
	}

	inner class SingleImageMediaItemViewHolder(binding: SingleImageItemBinding) : BaseMediaItemViewHolder(binding) {
		init {
			binding.image.setOnClickListener { showMediaItemFullScreen(currentList) }
			binding.image.setOnCreateContextMenuListener { menu, view, _ ->
				showContextMenu(menu, view, bindingAdapterPosition)
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseMediaItemViewHolder = when (viewType) {
		SINGLE_IMAGE -> {
			val binding = SingleImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			SingleImageMediaItemViewHolder(binding)
		}
		MULTIPLE_IMAGES -> {
			val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
			ImageMediaItemViewHolder(binding).apply {
				setOnClickListener { position, _ -> showMediaItemFullScreen(currentList, position) }
			}
		}
		else -> throw Exception("Unknown view type.")
	}

	override fun onBindViewHolder(holder: BaseMediaItemViewHolder, position: Int) {
		val mediaItem = getItem(position)
		holder.bind(mediaItem)
		printDebug("MEDIA_ITEM", mediaItem)
	}

	override fun getItemViewType(position: Int): Int {
		val mediaItem = getItem(position)
		return when {
			mediaItem.mimeType?.startsWith("image") == true ->
				if (currentList.isSingleton()) SINGLE_IMAGE else MULTIPLE_IMAGES
			else -> -1
		}
	}

	private fun showContextMenu(menu: Menu, view: View, position: Int) {
		menu.run {
			add(R.string.copy_item).setOnMenuItemClickListener {
				copyCallback.invoke(position); true
			}
			add(R.string.add_alt_text).setOnMenuItemClickListener {
				showAltTextDialog(position, view); true
			}
			add(R.string.delete_item).setOnMenuItemClickListener {
				deleteCallback.invoke(position); true
			}
			add(R.string.delete_all_items).setOnMenuItemClickListener {
				deleteAllCallback.invoke(); true
			}
		}
	}

	@SuppressLint("InflateParams")
	private fun showAltTextDialog(position: Int, view: View) {
		val parentView = LayoutInflater.from(view.context).inflate(R.layout.dialog_image_description, null)
		val imageDesc = parentView.findViewById<EditText>(R.id.image_desc).apply {
			setText(getItem(position).description)
		}
		MaterialAlertDialogBuilder(view.context, R.style.MyThemeOverlay_MaterialAlertDialog)
			.setTitle(R.string.alt_text)
			.setView(parentView)
			.setNegativeButton(R.string.cancel_button, null)
			.setPositiveButton(R.string.save_button) { _, _ ->
				onAltTextCallback.invoke(position, imageDesc.text.toString())
			}
			.create().apply {
				window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
				show()
			}
	}

	fun setOnCopyItemListener(callback: (position: Int) -> Unit) {
		copyCallback = callback
	}

	fun setOnDeleteItemListener(callback: (position: Int) -> Unit) {
		deleteCallback = callback
	}

	fun setOnDeleteAllListener(callback: () -> Unit) {
		deleteAllCallback = callback
	}

	fun setOnAltTextListener(callback: (position: Int, contentDescription: String) -> Unit) {
		onAltTextCallback = callback
	}

	private var deleteCallback: (position: Int) -> Unit = {}
	private var copyCallback: (position: Int) -> Unit = {}
	private var deleteAllCallback: () -> Unit = {}
	private var onAltTextCallback: (position: Int, contentDescription: String) -> Unit = { _, _ -> }

	private class MediaItemAdapterCallback : DiffUtil.ItemCallback<ImageEntity>() {

		override fun areItemsTheSame(oldItem: ImageEntity, newItem: ImageEntity) = oldItem.id == newItem.id

		override fun areContentsTheSame(oldItem: ImageEntity, newItem: ImageEntity) = oldItem == newItem
	}

	companion object {
		private const val SINGLE_IMAGE = R.layout.single_image_item
		private const val MULTIPLE_IMAGES = R.layout.image_item
	}
}