package io.github.xamdr.noties.ui.editor

import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.xamdr.noties.databinding.FragmentEditorTextBinding
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.SpanSizeLookupOwner
import io.github.xamdr.noties.ui.helpers.showSoftKeyboard

class EditorTextAdapter(
	private val note: Note,
	private val noteContentListener : NoteContentListener,
	private val listener: LinkClickedListener
	) : RecyclerView.Adapter<EditorTextAdapter.EditorTextViewHolder>(),
	SpanSizeLookupOwner {

	inner class EditorTextViewHolder(private val binding: FragmentEditorTextBinding) : RecyclerView.ViewHolder(binding.root), TextWatcher {

		private val contentReceiverListener = ImageContentReceiverListener { uri, _ ->
			onContentReceivedCallback(uri)
		}

		init {
			if (note.id == 0L) {
				binding.editor.post { binding.editor.showSoftKeyboard() }
			}
			binding.editor.setOnLinkClickedListener { url -> listener.onLinkClicked(url) }
			ViewCompat.setOnReceiveContentListener(
				binding.editor,
				ImageContentReceiverListener.MIME_TYPES,
				contentReceiverListener
			)
			binding.editor.addTextChangedListener(this)
		}

		fun bind(note: Note) {
			binding.editor.setText(note.text)
		}

		fun clear() = binding.editor.removeTextChangedListener(this)

		override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

		override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

		override fun afterTextChanged(s: Editable) {
			noteContentListener.onTextChanged(s.toString())
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

	override fun onViewDetachedFromWindow(holder: EditorTextViewHolder) {
		super.onViewDetachedFromWindow(holder)
		holder.clear()
	}

	override fun getItemCount() = 1

	override fun getSpanSizeLookup() = object : GridLayoutManager.SpanSizeLookup() {
		override fun getSpanSize(position: Int) = EditorFragment.SPAN_COUNT
	}

	fun setOnContentReceivedListener(callback: (uri: Uri) -> Unit) {
		onContentReceivedCallback = callback
	}

	private var onContentReceivedCallback: (uri: Uri) -> Unit = {}

	interface NoteContentListener {
		fun onTextChanged(text: String);
	}
}