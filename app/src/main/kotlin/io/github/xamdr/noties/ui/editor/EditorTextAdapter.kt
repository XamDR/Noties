package io.github.xamdr.noties.ui.editor

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.postDelayed
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.xamdr.noties.databinding.ItemTextBinding
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EditorTextAdapter(private var note: Note, private val listener: NoteContentListener) :
	RecyclerView.Adapter<EditorTextAdapter.EditorTextViewHolder>(), SpanSizeLookupOwner {

	private val handler = Handler(Looper.getMainLooper())

	inner class EditorTextViewHolder(private val binding: ItemTextBinding) :
		RecyclerView.ViewHolder(binding.root), TextWatcher {

		private val contentReceiverListener = ImageContentReceiverListener { uri, _ ->
			onContentReceivedCallback(uri)
		}
		private lateinit var runnable: Runnable

		init {
			if (note.id == 0L && note.isEmpty()) {
				binding.root.post { binding.text.showSoftKeyboard() }
			}
			binding.text.setOnLinkClickedListener { url -> listener.onLinkClicked(url) }
			ViewCompat.setOnReceiveContentListener(
				binding.text,
				ImageContentReceiverListener.MIME_TYPES,
				contentReceiverListener
			)
		}

		fun bind(note: Note) {
			val text = note.text
			if (text.length > TEXT_CHUNK_SIZE) {
				listener.onNoteContentLoading()
				val chunks = mutableListOf<String>()
				for	(i in 0..text.length step TEXT_CHUNK_SIZE) {
					val end = minOf(i + TEXT_CHUNK_SIZE, text.length)
					chunks.add(text.substring(i, end))
				}
				CoroutineScope(Dispatchers.Main).launch {
					for (chunk in chunks) {
						binding.text.append(chunk)
						delay(100)
					}
					binding.text.setSelection(0)
					listener.onNoteContentLoaded()
				}
			}
			else {
				binding.text.setText(text)
			}
		}

		fun bindTextWatcher() {
			binding.text.addTextChangedListener(this)
		}

		fun unbindTextWatcher() {
			binding.text.removeTextChangedListener(this)
		}

		override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

		override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
			if (::runnable.isInitialized) {
				handler.removeCallbacks(runnable)
			}
		}

		override fun afterTextChanged(s: Editable) {
			runnable = handler.postDelayed(DELAY) {
				listener.onNoteTextChanged(s.toString())
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorTextViewHolder {
		val binding = ItemTextBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false
		)
		return EditorTextViewHolder(binding)
	}

	override fun onBindViewHolder(holder: EditorTextViewHolder, position: Int) {
		holder.bind(note)
	}

	override fun onViewAttachedToWindow(holder: EditorTextViewHolder) {
		super.onViewAttachedToWindow(holder)
		holder.bindTextWatcher()
	}

	override fun onViewDetachedFromWindow(holder: EditorTextViewHolder) {
		super.onViewDetachedFromWindow(holder)
		holder.unbindTextWatcher()
	}

	override fun getItemCount() = 1

	override fun getSpanSizeLookup() = object : GridLayoutManager.SpanSizeLookup() {
		override fun getSpanSize(position: Int) = Constants.SPAN_COUNT
	}

	fun submitNote(note: Note) {
		this.note = note
		notifyItemChanged(0)
	}

	fun setOnContentReceivedListener(callback: (uri: Uri) -> Unit) {
		onContentReceivedCallback = callback
	}

	private var onContentReceivedCallback: (uri: Uri) -> Unit = {}

	private companion object {
		private const val DELAY = 1000L
		private const val TEXT_CHUNK_SIZE = 32768
	}
}