package io.github.xamdr.noties.ui.editor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import io.github.xamdr.noties.R
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.databinding.FragmentEditorBinding
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.domain.model.Task
import io.github.xamdr.noties.ui.editor.media.MediaItemAdapter
import io.github.xamdr.noties.ui.editor.media.RecordVideoLauncher
import io.github.xamdr.noties.ui.editor.media.TakePictureLauncher
import io.github.xamdr.noties.ui.editor.tags.ChipTagAdapter
import io.github.xamdr.noties.ui.editor.tags.EditorFooterAdapter
import io.github.xamdr.noties.ui.editor.tasks.DragDropCallback
import io.github.xamdr.noties.ui.editor.tasks.TaskAdapter
import io.github.xamdr.noties.ui.helpers.BitmapCache
import io.github.xamdr.noties.ui.helpers.ConcatSpanSizeLookup
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.DateTimeHelper
import io.github.xamdr.noties.ui.helpers.PermissionLauncher
import io.github.xamdr.noties.ui.helpers.ProgressDialogHelper
import io.github.xamdr.noties.ui.helpers.ShareHelper
import io.github.xamdr.noties.ui.helpers.SimpleTextWatcher
import io.github.xamdr.noties.ui.helpers.UriHelper
import io.github.xamdr.noties.ui.helpers.activityResultRegistry
import io.github.xamdr.noties.ui.helpers.addItemTouchHelper
import io.github.xamdr.noties.ui.helpers.addMenuProvider
import io.github.xamdr.noties.ui.helpers.getParcelableArrayListCompat
import io.github.xamdr.noties.ui.helpers.getParcelableCompat
import io.github.xamdr.noties.ui.helpers.getThemeColor
import io.github.xamdr.noties.ui.helpers.hideSoftKeyboard
import io.github.xamdr.noties.ui.helpers.isActive
import io.github.xamdr.noties.ui.helpers.launch
import io.github.xamdr.noties.ui.helpers.media.MediaHelper
import io.github.xamdr.noties.ui.helpers.media.MediaStorageManager
import io.github.xamdr.noties.ui.helpers.onBackPressed
import io.github.xamdr.noties.ui.helpers.showDialog
import io.github.xamdr.noties.ui.helpers.showOnTop
import io.github.xamdr.noties.ui.helpers.showSnackbar
import io.github.xamdr.noties.ui.helpers.showSnackbarWithAction
import io.github.xamdr.noties.ui.helpers.showToast
import io.github.xamdr.noties.ui.helpers.simpleName
import io.github.xamdr.noties.ui.media.MediaViewerActivity
import io.github.xamdr.noties.ui.reminders.AlarmManagerHelper
import io.github.xamdr.noties.ui.reminders.DateTimeListener
import io.github.xamdr.noties.ui.reminders.DateTimePickerDialogFragment
import io.github.xamdr.noties.ui.settings.PreferenceStorage
import timber.log.Timber
import java.io.FileNotFoundException
import java.time.Instant
import javax.inject.Inject
import com.google.android.material.R as Material

@AndroidEntryPoint
class EditorFragment : Fragment(), NoteContentListener, SimpleTextWatcher, EditorMenuListener, DateTimeListener {

	private var _binding: FragmentEditorBinding? = null
	private val binding get() = _binding!!
	private val viewModel by viewModels<EditorViewModel>()
	private val noteId by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getLong(Constants.BUNDLE_NOTE_ID, 0L)
	}
//	private val tagId by lazy(LazyThreadSafetyMode.NONE) {
//		requireArguments().getInt(Constants.BUNDLE_TAG_ID, 0)
//	}
	private lateinit var note: Note
	private lateinit var concatAdapter: ConcatAdapter
	private lateinit var contentAdapter: EditorContentAdapter
	private lateinit var taskAdapter: TaskAdapter
	private val mediaItemAdapter = MediaItemAdapter(this::navigateToMediaViewer)
	private val tagAdapter = ChipTagAdapter(this::showReminderDialog)
	private val footerAdapter = EditorFooterAdapter(tagAdapter)
	private val menuProvider = EditorMenuProvider()
	private val itemTouchHelper = ItemTouchHelper(DragDropCallback())
	private val pickeMediaLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
		addMediaItems(uris)
	}
	private lateinit var takePictureLauncher: TakePictureLauncher
	private lateinit var recordVideoLauncher: RecordVideoLauncher
	private val openFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
		readFileContent(uri)
	}
	private var fileUri: Uri? = null
	private val itemsToDelete = mutableListOf<MediaItem>()
	private val mediaViewerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			handleItemsToDelete(result.data)
		}
	}
	private lateinit var postNotificationPermissionLauncher: PermissionLauncher
	@Inject lateinit var preferenceStorage: PreferenceStorage
	private var titleInEditMode = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		takePictureLauncher = TakePictureLauncher(
			requireContext(),
			activityResultRegistry,
			this,
			onSuccess = { cameraUri -> addMediaItems(listOf(cameraUri)) },
			onError = { binding.root.showSnackbar(R.string.error_take_picture) }
		)
		recordVideoLauncher = RecordVideoLauncher(
			requireContext(),
			activityResultRegistry,
			this,
			onSuccess = { videoUri -> addMediaItems(listOf(videoUri)) },
			onError = { binding.root.showSnackbar(R.string.error_take_video) }
		)
		postNotificationPermissionLauncher = PermissionLauncher(
			requireContext(),
			activityResultRegistry,
			Constants.NOTIFICATION_PERMISSION_KEY,
			onPermissionGranted = { showReminderDialog() },
			onPermissionDenied = { requireContext().showToast(R.string.permission_denied) }
		)
		lifecycle.addObserver(takePictureLauncher)
		lifecycle.addObserver(recordVideoLauncher)
		lifecycle.addObserver(postNotificationPermissionLauncher)
	}

	override fun onCreateView(inflater: LayoutInflater,
							  container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		_binding = FragmentEditorBinding.inflate(inflater, container, false)
		initTransitions(noteId)
		postponeEnterTransition()
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		binding.title.removeTextChangedListener(this)
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupNote()
		setupListeners()
		onBackPressed { saveNoteOnBackPress() }
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		if (::note.isInitialized && note.text.length <= Constants.MAX_TEXT_LIMIT) {
			viewModel.saveState(note)
		}
		else {
			Timber.d("Text is too big to be saved into bundle.")
			fileUri?.let { outState.putParcelable(Constants.BUNDLE_FILE_URI, it) }
		}
	}

	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		super.onViewStateRestored(savedInstanceState)
		if (savedInstanceState != null && savedInstanceState.containsKey(Constants.BUNDLE_FILE_URI)) {
			fileUri = savedInstanceState.getParcelableCompat(Constants.BUNDLE_FILE_URI, Uri::class.java)
			readFileContent(fileUri)
		}
	}

	override fun onNoteTextChanged(text: String) {
		note = note.copy(text = text)
		requireActivity().invalidateMenu()
	}

	override fun onNoteContentLoading() = ProgressDialogHelper.show(requireContext(), getString(R.string.loading_text))

	override fun onNoteContentLoaded() = ProgressDialogHelper.dismiss()

	override fun onLinkClicked(url: String) {
		binding.root.showSnackbarWithAction(url, actionText = R.string.open_url) {
			startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
		}
	}

	override fun afterTextChanged(s: Editable) {
		note = note.copy(title = s.toString())
	}

	override fun onAttachMediaFiles() = pickeMediaLauncher.launch(arrayOf("image/*", "video/*"))

	override fun onTakePicture() = takePictureLauncher.launch()

	override fun onTakeVideo() = recordVideoLauncher.launch()

	override fun onAddTaskList() {
		if (concatAdapter.removeAdapter(contentAdapter)) {
			val tasks = (note.toTaskList() + Task.Footer).toMutableList()
			taskAdapter.submitTasks(tasks)
			concatAdapter.addAdapter(taskAdapter)
			note = note.copy(isTaskList = true)
			viewModel.isTaskList.value = true
			requireActivity().invalidateMenu()
		}
	}

	override fun onAddReminder() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			postNotificationPermissionLauncher.executeOrLaunch(
				Manifest.permission.POST_NOTIFICATIONS,
				R.string.post_notifications_permission_rationale,
				R.drawable.ic_reminder
			)
		}
		else {
			showReminderDialog()
		}
	}

	override fun onReminderDateSet(dateTime: Instant) {
		val value = dateTime.toEpochMilli()
		val tag = Tag(name = DateTimeHelper.formatDateTime(value))
		if (!concatAdapter.adapters.contains(footerAdapter)) {
			concatAdapter.addAdapter(footerAdapter)
		}
		tagAdapter.submitList(listOf(tag))
		note = note.copy(reminderDate = value)
	}

	override fun onReminderDateDeleted() {
		tagAdapter.removeTagAt(0)
		note = note.copy(reminderDate = null)
		AlarmManagerHelper.cancelAlarm(requireContext(), note.id)
	}

	private fun navigateUp() {
		binding.root.hideSoftKeyboard()
		onBackPressed()
	}

	private fun setupNote() {
		launch {
			if (!::note.isInitialized) {
				note = viewModel.getNote(noteId)
				viewModel.isTaskList.value = note.isTaskList
			}
			concatAdapter = initializeAdapter(note)
			setupViews(note)
		}
	}

	private fun initializeAdapter(note: Note): ConcatAdapter {
		val tasks = (note.toTaskList() + Task.Footer).toMutableList()
		taskAdapter = TaskAdapter(this, tasks, itemTouchHelper)
		contentAdapter = EditorContentAdapter(note, this@EditorFragment).apply {
			setOnContentReceivedListener { uri -> addMediaItems(listOf(uri)) }
		}
		return if (note.isTaskList) ConcatAdapter(mediaItemAdapter, taskAdapter)
			else ConcatAdapter(mediaItemAdapter, contentAdapter)
	}

	private fun setupViews(note: Note) {
//		supportActionBar?.title = note.title.ifEmpty { getString(R.string.editor_fragment_label) }
		binding.content.apply {
			adapter = concatAdapter
			(layoutManager as GridLayoutManager).spanSizeLookup =
				ConcatSpanSizeLookup(Constants.SPAN_COUNT) { concatAdapter.adapters }
			addItemTouchHelper(itemTouchHelper)
		}
		mediaItemAdapter.submitList(note.items)
		if (note.reminderDate != null) {
			concatAdapter.addAdapter(footerAdapter)
			val tag = Tag(name = DateTimeHelper.formatDateTime(note.reminderDate))
			tagAdapter.submitList(listOf(tag))
		}
		binding.modificationDate.text = if (note.modificationDate == 0L) DateTimeHelper.formatDateTime(Instant.now().toEpochMilli())
			else DateTimeHelper.formatDateTime(note.modificationDate)
		binding.root.doOnPreDraw { startPostponedEnterTransition() }
		addMenuProvider(menuProvider, viewLifecycleOwner)
	}

	private fun setupListeners() {
		binding.buttonAdd.setOnClickListener {
			val menuDialog = EditorMenuFragment().apply {
				setEditorMenuListener(this@EditorFragment)
			}
			showDialog(menuDialog, Constants.MENU_DIALOG_TAG)
		}
//		requireActivity().findViewById<MaterialToolbar>(R.id.toolbar).apply {
//			setOnClickListener {
//				titleInEditMode = true
//				binding.textInputLayout.slideVisibility(true, Gravity.TOP)
//				binding.title.showSoftKeyboard()
//				binding.title.setText(note.title)
//				supportActionBar?.title = String.Empty
//				supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_upward)
//			}
//			setNavigationOnClickListener {
//				if (titleInEditMode) {
//					titleInEditMode = false
//					binding.textInputLayout.slideVisibility(false, Gravity.TOP)
//					supportActionBar?.title = note.title.ifEmpty { getString(R.string.editor_fragment_label) }
//					supportActionBar?.setHomeAsUpIndicator(0)
//					if (note.id == 0L) binding.content.showSoftKeyboard() else binding.title.hideSoftKeyboard()
//				}
//				else {
//					navigateUp()
//				}
//			}
//		}
		binding.title.addTextChangedListener(this)
	}

	private fun initTransitions(noteId: Long) {
		if (noteId == 0L) {
			enterTransition = MaterialContainerTransform().apply {
				startView = requireActivity().findViewById(R.id.fab)
				addTarget(binding.root)
				endContainerColor = requireContext().getThemeColor(R.attr.colorSurface)
				setAllContainerColors(MaterialColors.getColor(binding.root, Material.attr.colorSurface))
				setPathMotion(MaterialArcMotion())
				duration = resources.getInteger(R.integer.motion_duration_large).toLong()
				interpolator = FastOutSlowInInterpolator()
				fadeMode = MaterialContainerTransform.FADE_MODE_IN
			}
		}
		else {
			binding.root.transitionName = noteId.toString()
			sharedElementEnterTransition = MaterialContainerTransform().apply {
				drawingViewId = R.id.nav_host_fragment
				duration = resources.getInteger(R.integer.motion_duration_large).toLong()
				interpolator = FastOutSlowInInterpolator()
				fadeMode = MaterialContainerTransform.FADE_MODE_IN
				setAllContainerColors(MaterialColors.getColor(binding.root, Material.attr.colorSurface))
			}
		}
	}

	private fun saveNoteOnBackPress() {
		if (::note.isInitialized) {
			if (note.isTaskList) {
				note = note.copy(text = taskAdapter.convertItemsToString())
			}
			saveNote(note)
		}
	}

	private fun saveNote(note: Note) {
		launch {
			Timber.d("Note: %s", note)
			when (viewModel.saveNote(note, noteId)) {
				NoteAction.DeleteEmptyNote -> {
					if (note.reminderDate != null) {
						AlarmManagerHelper.cancelAlarm(requireContext(), note.id)
					}
					MediaStorageManager.deleteItems(requireContext(), note.items)
					binding.root.showSnackbar(R.string.empty_note_deleted).showOnTop()
				}
				NoteAction.InsertNote -> {
					if (note.reminderDate != null) {
						AlarmManagerHelper.setAlarm(requireContext(), note, preferenceStorage.isExactAlarmEnabled)
					}
					binding.root.showSnackbar(R.string.note_saved).showOnTop()
				}
				NoteAction.NoAction -> {}
				NoteAction.UpdateNote -> {
					if (note.reminderDate != null) {
						AlarmManagerHelper.setAlarm(requireContext(), note, preferenceStorage.isExactAlarmEnabled)
					}
					MediaStorageManager.deleteItems(requireContext(), itemsToDelete)
					viewModel.deleteItems(itemsToDelete)
					binding.root.showSnackbar(R.string.note_updated).showOnTop()
				}
			}
			findNavController().popBackStack()
		}
	}

	private fun addMediaItems(uris: List<Uri>) {
		if (uris.isEmpty()) return
		launch {
			binding.progressIndicator.show()
			val items = mutableListOf<MediaItem>()
			for (uri in uris) {
				val newUri = MediaHelper.copyUri(requireContext(), uri)
				val mimeType = MediaHelper.getMediaMimeType(requireContext(), newUri)
				val item: MediaItem
				if (MediaHelper.isImage(requireContext(), newUri)) {
					item = MediaItem(
						uri = newUri,
						mimeType = mimeType,
						mediaType = MediaType.Image,
						noteId = note.id
					)
				}
				else {
					val metadata = MediaHelper.getMediaItemMetadata(requireContext(), newUri)
					item = MediaItem(
						uri = newUri,
						mimeType = mimeType,
						mediaType = MediaType.Video,
						metadata = metadata,
						noteId = note.id
					)
				}
				items.add(item)
			}
			note = note.copy(items = note.items + items)
			mediaItemAdapter.submitList(note.items)
			binding.progressIndicator.hide()
			requireActivity().invalidateMenu()
		}
	}

	private fun navigateToMediaViewer(view: View, position: Int) {
		BitmapCache.Instance.clear()
		val intent = Intent(requireContext(), MediaViewerActivity::class.java).apply {
			putExtra(Constants.BUNDLE_ITEMS, ArrayList(note.items))
			putExtra(Constants.BUNDLE_POSITION, position)
		}
		mediaViewerLauncher.launch(intent)
	}

	private fun handleItemsToDelete(data: Intent?) {
		val items = data?.getParcelableArrayListCompat(Constants.BUNDLE_ITEMS_DELETE, MediaItem::class.java) ?: return
		itemsToDelete.addAll(items)
		note = note.copy(items = note.items - items.toSet())
		mediaItemAdapter.submitList(note.items)
		requireActivity().invalidateMenu()
	}

	private fun readFileContent(uri: Uri?) {
		if (uri != null) {
			try {
				launch {
					fileUri = uri
					val file = DocumentFile.fromSingleUri(requireContext(), uri)
					val text = UriHelper.readTextFromUri(requireContext(), uri)
					note = note.copy(title = file?.simpleName ?: String.Empty, text = text)
//					supportActionBar?.title = note.title
					contentAdapter.submitNote(note)
					requireActivity().invalidateMenu()
				}
			}
			catch (e: FileNotFoundException) {
				Timber.e(e)
				binding.root.showSnackbar(R.string.error_open_file)
			}
		}
	}

	private fun removeTaskList() {
		if (concatAdapter.removeAdapter(taskAdapter)) {
			note = note.copy(text = taskAdapter.joinToString(), isTaskList =  false)
			viewModel.isTaskList.value = false
			contentAdapter.submitNote(note)
			concatAdapter.addAdapter(contentAdapter)
			requireActivity().invalidateMenu()
		}
	}

	private fun checkAllTasks() = taskAdapter.markAllTasksAsDone(true)

	private fun uncheckAllTasks() = taskAdapter.markAllTasksAsDone(false)

	private fun showReminderDialog() {
		val reminderDate = note.reminderDate ?: 0L
		val dateTimePickerDialog = DateTimePickerDialogFragment.newInstance(reminderDate).apply {
			listener = this@EditorFragment
		}
		showDialog(dateTimePickerDialog, Constants.DATE_TIME_PICKER_DIALOG_TAG)
	}

	private inner class EditorMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menuInflater.inflate(R.menu.menu_editor, menu)
		}

		override fun onPrepareMenu(menu: Menu) {
			menu.findItem(R.id.share_content).isActive = !note.isEmpty()
			menu.findItem(R.id.open_file).isActive = !note.isTaskList
			menu.findItem(R.id.hide_tasks).isActive = note.isTaskList
			menu.findItem(R.id.check_tasks).isActive = note.isTaskList
			menu.findItem(R.id.uncheck_tasks).isActive = note.isTaskList
		}

		override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
			return when (menuItem.itemId) {
				android.R.id.home -> {
					navigateUp(); true
				}
				R.id.share_content -> {
					ShareHelper.shareContent(requireContext(), note); true
				}
				R.id.open_file -> {
					binding.root.hideSoftKeyboard()
					openFileLauncher.launch(arrayOf(Constants.MIME_TYPE_TEXT)); true
				}
				R.id.hide_tasks -> {
					removeTaskList(); true
				}
				R.id.check_tasks -> {
					checkAllTasks(); true
				}
				R.id.uncheck_tasks -> {
					uncheckAllTasks(); true
				}
				else -> false
			}
		}
	}
}