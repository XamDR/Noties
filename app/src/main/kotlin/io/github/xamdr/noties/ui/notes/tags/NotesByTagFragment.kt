package io.github.xamdr.noties.ui.notes.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment

class NotesByTagFragment : Fragment() {

	//	private val isRecycleBin by lazy(LazyThreadSafetyMode.NONE) {
//		requireArguments().getBoolean(Constants.BUNDLE_RECYCLE_BIN, false)
//	}
//	private val menuProvider = RecycleBinMenuProvider()
//	private lateinit var trashedNotes: List<Note>

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent {  }
		}
	}

	//	private fun buildUIForTrashedNotes(trashedNotes: List<Note>) {
//		binding.fab.isVisible = false
//		supportActionBar?.show(getString(R.string.recycle_bin_fragment_label))
//		if (trashedNotes.isNotEmpty()) addMenuProvider(menuProvider, viewLifecycleOwner)
//		binding.emptyView.setText(R.string.empty_recycle_bin)
//	}

//	private fun showEmptyRecycleBinDialog() {
//		val dialog = EmptyRecycleBinDialogFragment().apply {
//			setOnRecycleBinEmptyListener {
//				this@NotesByTagFragment.launch {
//					if (::trashedNotes.isInitialized) {
//						viewModel.emptyRecycleBin(trashedNotes)
//						deleteImages(trashedNotes)
//						this@NotesFragment.removeMenuProvider(menuProvider)
//					}
//				}
//			}
//		}
//		showDialog(dialog, Constants.EMPTY_RECYCLE_BIN_DIALOG_TAG)
//	}

//	private inner class RecycleBinMenuProvider : MenuProvider {
//		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//			menuInflater.inflate(R.menu.menu_recycle_bin, menu)
//		}
//
//		override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
//			R.id.empty_recycle_bin -> {
//				showEmptyRecycleBinDialog(); true
//			}
//			else -> false
//		}
//	}
}