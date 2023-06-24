package io.github.xamdr.noties.ui.media

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.print.PrintHelper
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.*
import io.github.xamdr.noties.ui.helpers.media.MediaHelper
import io.github.xamdr.noties.ui.helpers.media.MediaStorageManager
import timber.log.Timber
import java.io.FileNotFoundException

open class MediaViewerFragment : Fragment() {

	protected val item by lazy(LazyThreadSafetyMode.NONE) {
		requireArguments().getParcelableCompat(Constants.BUNDLE_MEDIA_ITEM, MediaItem::class.java)
	}
	protected val fullScreenHelper = FullScreenHelper(
		onEnterFullScreen = { supportActionBar?.hide() },
		onExitFullScreen = { supportActionBar?.show() }
	)

	protected fun shareMediaItem() {
		val uri = item.uri ?: return
		ShareCompat.IntentBuilder(requireContext())
			.setType(Constants.MIME_TYPE_IMAGE)
			.addStream(uri)
			.setChooserTitle(R.string.share_item)
			.startChooser()
	}

	protected fun rotateScreen() {
		requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
	}

	protected fun copyImageToClipboard() {
		val uri = item.uri ?: return
		requireContext().copyUriToClipboard(R.string.image_item, uri, R.string.image_copied_msg)
	}

	protected fun downloadMediaItem() {
		val uri = item.uri ?: return
		launch {
			if	(MediaHelper.isVideo(requireContext(), uri)) {
				ProgressDialogHelper.show(requireContext(), getString(R.string.download_video_message), true)
				MediaStorageManager.downloadVideo(requireContext(), uri)
				ProgressDialogHelper.dismiss()
				requireContext().showToast(R.string.video_downloaded_message)
			}
			else {
				MediaStorageManager.downloadPicture(requireContext(), uri)
				requireContext().showToast(R.string.image_downloaded_message)
			}
		}
	}

	protected fun printImage() {
		val suffix = (0..999).random()
		val jobName = getString(R.string.print_image_job_name, suffix)
		val printHelper = PrintHelper(requireContext()).apply { scaleMode = PrintHelper.SCALE_MODE_FILL }
		try {
			requireContext().showToast(R.string.init_print_dialog)
			val imageFile = item.uri ?: return
			printHelper.printBitmap(jobName, imageFile)
		}
		catch (e: FileNotFoundException) {
			Timber.e(e)
			requireContext().showToast(R.string.error_print_image)
		}
	}

	protected fun setImageAs() {
		val uri = item.uri ?: return
		val intent = Intent().apply {
			action = Intent.ACTION_ATTACH_DATA
			setDataAndType(uri, Constants.MIME_TYPE_IMAGE)
			putExtra("mimeType", Constants.MIME_TYPE_IMAGE)
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		startActivity(Intent.createChooser(intent, getString(R.string.set_image_as)))
	}
}