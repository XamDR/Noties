package io.github.xamdr.noties.ui.media

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.annotation.StringRes
import androidx.core.app.ShareCompat
import androidx.print.PrintHelper
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.ProgressDialogHelper
import io.github.xamdr.noties.ui.helpers.copyUriToClipboard
import io.github.xamdr.noties.ui.helpers.isLandscape
import io.github.xamdr.noties.ui.helpers.media.MediaHelper
import io.github.xamdr.noties.ui.helpers.media.MediaStorageManager
import io.github.xamdr.noties.ui.helpers.showToast
import timber.log.Timber
import java.io.FileNotFoundException

data class ActionItem(
	@StringRes val title: Int,
	val action: () -> Unit
)

fun shareMediaItem(item: MediaItem, context: Context) {
	ShareCompat.IntentBuilder(context)
		.setType(Constants.MIME_TYPE_IMAGE)
		.addStream(item.uri)
		.setChooserTitle(R.string.share_item)
		.startChooser()
}

fun toggleScreenOrientation(context: Context, activity: Activity) {
	val newOrientation = if (context.isLandscape()) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
		else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
	activity.requestedOrientation = newOrientation
}

fun copyImageToClipboard(item: MediaItem, context: Context) {
	context.copyUriToClipboard(R.string.image_item, item.uri, R.string.image_copied_msg)
}

suspend fun downloadMediaItem(item: MediaItem, context: Context) {
	val uri = item.uri
	if	(MediaHelper.isVideo(context, uri)) {
		ProgressDialogHelper.show(context, R.string.download_video_message, cancelable = true)
		MediaStorageManager.downloadVideo(context, uri)
		ProgressDialogHelper.dismiss()
		context.showToast(R.string.video_downloaded_message)
	}
	else {
		MediaStorageManager.downloadPicture(context, uri)
		context.showToast(R.string.image_downloaded_message)
	}
}

fun printImage(item: MediaItem, context: Context) {
	val suffix = (0..999).random()
	val jobName = context.getString(R.string.print_image_job_name, suffix)
	val printHelper = PrintHelper(context).apply { scaleMode = PrintHelper.SCALE_MODE_FILL }
	try {
		context.showToast(R.string.init_print_dialog)
		printHelper.printBitmap(jobName, item.uri)
	}
	catch (e: FileNotFoundException) {
		Timber.e(e)
		context.showToast(R.string.error_print_image)
	}
}

fun setImageAs(item: MediaItem, context: Context) {
	val intent = Intent().apply {
		action = Intent.ACTION_ATTACH_DATA
		setDataAndType(item.uri, Constants.MIME_TYPE_IMAGE)
		putExtra("mimeType", Constants.MIME_TYPE_IMAGE)
		addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
	}
	context.startActivity(Intent.createChooser(intent, context.getString(R.string.set_image_as)))
}