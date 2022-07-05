package net.azurewebsites.noties.ui.editor

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.databinding.FragmentEditorContentBinding
import net.azurewebsites.noties.ui.helpers.showSoftKeyboard

class EditorContentAdapter(
	private val note: Note,
	private val listener: LinkClickedListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	inner class EditorViewHolder(private val binding: FragmentEditorContentBinding) : RecyclerView.ViewHolder(binding.root) {

		private val contentReceiverListener = ImageContentReceiverListener { uri, _ ->
			onContentReceivedCallback(uri)
		}

		init {
			if (note.entity.id == 0L) {
				binding.editor.post { binding.editor.showSoftKeyboard() }
			}
			binding.editor.setOnLinkClickedListener { url -> listener.onLinkClicked(url) }
			ViewCompat.setOnReceiveContentListener(
				binding.editor,
				ImageContentReceiverListener.MIME_TYPES,
				contentReceiverListener
			)
		}

		fun bind(note: Note) {
			binding.note = note
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when(viewType) {
		EDITOR -> {
			val binding = FragmentEditorContentBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
			EditorViewHolder(binding)
		}
		else -> throw Exception("Unknown view type.")
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		if (holder is EditorViewHolder) {
			holder.bind(note)
		}
	}

	override fun getItemCount() = 1

	override fun getItemViewType(position: Int) = EDITOR

	fun setOnContentReceivedListener(callback: (uri: Uri) -> Unit) {
		onContentReceivedCallback = callback
	}

	private var onContentReceivedCallback: (uri: Uri) -> Unit = {}

	private companion object {
		private const val EDITOR = R.layout.fragment_editor_content
	}
}