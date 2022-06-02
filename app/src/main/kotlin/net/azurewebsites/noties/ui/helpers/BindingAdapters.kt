package net.azurewebsites.noties.ui.helpers

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.databinding.BindingAdapter
import net.azurewebsites.noties.ui.image.BitmapCache
import net.azurewebsites.noties.ui.image.ImageStorageManager
import net.azurewebsites.noties.ui.views.CollectionView
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

@BindingAdapter("emptyView")
fun bindEmptyView(collectionView: CollectionView, @IdRes viewId: Int) {
	val emptyView = collectionView.rootView.findViewById<View>(viewId)
	collectionView.setEmptyView(emptyView)
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