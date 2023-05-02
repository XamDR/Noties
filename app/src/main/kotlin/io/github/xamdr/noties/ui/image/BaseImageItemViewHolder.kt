package io.github.xamdr.noties.ui.image

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import io.github.xamdr.noties.databinding.ItemImageBinding
import io.github.xamdr.noties.databinding.ItemSingleImageBinding
import io.github.xamdr.noties.domain.model.Image
import pl.droidsonroids.gif.GifDrawable
import java.util.concurrent.Executors

open class BaseImageItemViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

	private val executor = Executors.newSingleThreadExecutor()
	private val handler = Handler(Looper.getMainLooper())

	fun bind(image: Image) {
		when (binding) {
			is ItemImageBinding -> bind(binding.image, image.uri, 200, 200)
			is ItemSingleImageBinding -> bind(binding.image, image.uri, 800, 800)
		}
	}

	private fun bind(imageView: ImageView, src: Uri?, width: Int, height: Int) {
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
}