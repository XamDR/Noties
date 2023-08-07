package io.github.xamdr.noties.ui.notes.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.FragmentNotesByTagBinding
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.addMenuProvider
import io.github.xamdr.noties.ui.helpers.launch
import io.github.xamdr.noties.ui.helpers.show
import io.github.xamdr.noties.ui.helpers.showDialog
import io.github.xamdr.noties.ui.helpers.supportActionBar
import io.github.xamdr.noties.ui.notes.recyclebin.EmptyRecycleBinDialogFragment

class NotesByTagFragment : Fragment() {

	private var _binding: FragmentNotesByTagBinding? = null
	private val binding get() = _binding!!
	private val isRecycleBin by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getBoolean(Constants.BUNDLE_RECYCLE_BIN, false)
	}
	private val menuProvider = RecycleBinMenuProvider()
	private lateinit var trashedNotes: List<Note>

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentNotesByTagBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun buildUIForTrashedNotes(trashedNotes: List<Note>) {
		binding.fab.isVisible = false
		supportActionBar?.show(getString(R.string.recycle_bin_fragment_label))
		if (trashedNotes.isNotEmpty()) addMenuProvider(menuProvider, viewLifecycleOwner)
		binding.emptyView.setText(R.string.empty_recycle_bin)
	}

	private fun showEmptyRecycleBinDialog() {
		val dialog = EmptyRecycleBinDialogFragment().apply {
			setOnRecycleBinEmptyListener {
				this@NotesByTagFragment.launch {
//					if (::trashedNotes.isInitialized) {
//						viewModel.emptyRecycleBin(trashedNotes)
//						deleteImages(trashedNotes)
//						this@NotesFragment.removeMenuProvider(menuProvider)
//					}
				}
			}
		}
		showDialog(dialog, Constants.EMPTY_RECYCLE_BIN_DIALOG_TAG)
	}

	private inner class RecycleBinMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menuInflater.inflate(R.menu.menu_recycle_bin, menu)
		}

		override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
			R.id.empty_recycle_bin -> {
				showEmptyRecycleBinDialog(); true
			}
			else -> false
		}
	}
}