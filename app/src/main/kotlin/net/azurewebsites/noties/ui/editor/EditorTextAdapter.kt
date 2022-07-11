package net.azurewebsites.noties.ui.editor

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.azurewebsites.noties.core.Note
import net.azurewebsites.noties.databinding.FragmentEditorTextBinding
import net.azurewebsites.noties.ui.helpers.SpanSizeLookupOwner
import net.azurewebsites.noties.ui.helpers.showSoftKeyboard

class EditorTextAdapter(
	private val note: Note,
	private val listener: LinkClickedListener
	) : RecyclerView.Adapter<EditorTextAdapter.EditorTextViewHolder>(),
		SpanSizeLookupOwner {

	inner class EditorTextViewHolder(private val binding: FragmentEditorTextBinding) : RecyclerView.ViewHolder(binding.root) {

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

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorTextViewHolder {
		val binding = FragmentEditorTextBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)
		return EditorTextViewHolder(binding)
	}

	override fun onBindViewHolder(holder: EditorTextViewHolder, position: Int) {
		holder.bind(note)
	}

	override fun getItemCount() = 1

	override fun getSpanSizeLookup() = object : GridLayoutManager.SpanSizeLookup() {
		override fun getSpanSize(position: Int) = EditorFragment.SPAN_COUNT
	}

	fun setOnContentReceivedListener(callback: (uri: Uri) -> Unit) {
		onContentReceivedCallback = callback
	}

	private var onContentReceivedCallback: (uri: Uri) -> Unit = {}
}