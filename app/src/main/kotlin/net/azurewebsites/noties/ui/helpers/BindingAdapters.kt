package net.azurewebsites.noties.ui.helpers

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.util.Linkify
import android.util.TypedValue
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import net.azurewebsites.noties.ui.image.BitmapCache
import net.azurewebsites.noties.ui.media.ImageStorageManager
import net.azurewebsites.noties.ui.views.LinedEditText
import net.azurewebsites.noties.util.EditorStyle
import pl.droidsonroids.gif.GifDrawable
import java.util.concurrent.Executors

@BindingAdapter("drawableSize")
fun bindDrawableSize(textView: TextView, size: Float) {
	val drawables = textView.compoundDrawables
	if (drawables[1] != null) {
		drawables[1].setBounds(0, 0, size.toInt(), size.toInt())
		textView.setCompoundDrawables(null, drawables[1], null, null)
	}
}

@BindingAdapter("android:autoLink")
fun bindAutolink(editText: EditText, isAutolinkEnabled: Boolean) {
	editText.autoLinkMask = if (isAutolinkEnabled) Linkify.WEB_URLS else 0
}

@BindingAdapter("android:textSize")
fun bindTextSize(editText: EditText, size: Float) {
	editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
}

@BindingAdapter("hasGridLines")
fun bindHasGridLines(editText: LinedEditText, value: String) {
	when (EditorStyle.from(value)) {
		EditorStyle.Blank -> editText.hasGridLines = false
		EditorStyle.Striped -> editText.hasGridLines = true
		else -> throw Exception("Unknown editor style.")
	}
}

private val executor = Executors.newSingleThreadExecutor()
private val handler = Handler(Looper.getMainLooper())

@BindingAdapter("source", "width", "height", requireAll = true)
fun bindImageSrc(imageView: ImageView, src: Uri?, width: Int, height: Int) {
	if (src != null) {
		val imageKey = src.toString()
		var bitmap = BitmapCache.Instance.getBitmapFromMemCache(imageKey)
		var gifDrawable: GifDrawable? = null
		val mimeType = imageView.context.contentResolver.getType(src)

		if (bitmap != null) {
			// This means there is a bitmap available in our cache, so we use it.
			imageView.setImageBitmap(bitmap)
		}
		else {
			// In this case there is not bitmap available, so in a background thread
			// we get the bitmap from the corresponding uri and then we add it to the cache.
			// Finally, we use the bitmap in the UI thread.
			executor.execute {
				if (mimeType == "image/gif") {
					gifDrawable = GifDrawable(imageView.context.contentResolver, src)
				}
				else {
					bitmap = ImageStorageManager.getImageFromInternalStorage(imageView.context, src, width, height)?.also {
						BitmapCache.Instance.addBitmapToMemoryCache(imageKey, it)
					}
				}
				handler.post {
					if (bitmap != null && mimeType != "image/gif") {
						imageView.setImageBitmap(bitmap)
					}
					else if (gifDrawable != null && mimeType == "image/gif") {
						imageView.setImageDrawable(gifDrawable)
					}
				}
			}
		}
	}
	else {
		imageView.setImageBitmap(null)
	}
}