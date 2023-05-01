package io.github.xamdr.noties.ui.gallery

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.print.PrintHelper
import androidx.viewpager2.widget.ViewPager2
import io.github.xamdr.noties.R
import io.github.xamdr.noties.databinding.ActivityGalleryBinding
import io.github.xamdr.noties.domain.model.Image
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.getParcelableArrayListCompat
import io.github.xamdr.noties.ui.helpers.showToast
import io.github.xamdr.noties.ui.image.BitmapCache
import timber.log.Timber
import java.io.FileNotFoundException

class GalleryActivity : AppCompatActivity() {

	private lateinit var binding: ActivityGalleryBinding
	private lateinit var stateAdapter: ViewPagerStateAdapter
	private val images by lazy(LazyThreadSafetyMode.NONE) {
		intent.getParcelableArrayListCompat(Constants.BUNDLE_IMAGES, Image::class.java)
	}
	private val position by lazy(LazyThreadSafetyMode.NONE) {
		intent.getIntExtra(Constants.BUNDLE_POSITION, 0)
	}
	private val callback = PageSelectedCallback()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityGalleryBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		setupViewPager()
	}

	override fun onStart() {
		super.onStart()
		callback.onPageSelected(position)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu_image_full_screen, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
		R.id.share_image -> {
			shareImage(binding.pager.currentItem); true
		}
		R.id.set_as -> {
			setImageAs(binding.pager.currentItem); true
		}
		R.id.print_image -> {
			printImage(binding.pager.currentItem); true
		}
		android.R.id.home -> {
			navigateUp(); true
		}
		else -> false
	}

	override fun onPause() {
		super.onPause()
		binding.pager.unregisterOnPageChangeCallback(callback)
	}

	override fun onResume() {
		super.onResume()
		binding.pager.registerOnPageChangeCallback(callback)
	}

	private fun shareImage(position: Int) {
		val shareIntent = Intent().apply {
			action = Intent.ACTION_SEND
			putExtra(Intent.EXTRA_STREAM, images[position].uri)
			type = MIME_TYPE
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)))
	}

	private fun setImageAs(position: Int) {
		val intent = Intent().apply {
			action = Intent.ACTION_ATTACH_DATA
			setDataAndType(images[position].uri, MIME_TYPE)
			putExtra("mimeType", MIME_TYPE)
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		startActivity(Intent.createChooser(intent, getString(R.string.set_image_as)))
	}

	private fun printImage(position: Int) {
		val jobName = getString(R.string.print_image_job_name, position)
		val printHelper = PrintHelper(this).apply { scaleMode = PrintHelper.SCALE_MODE_FILL }
		try {
			showToast(R.string.init_print_dialog)
			val imageFile = images[position].uri ?: return
			printHelper.printBitmap(jobName, imageFile)
		}
		catch (e: FileNotFoundException) {
			Timber.e(e)
			showToast(R.string.error_print_image)
		}
	}

	private fun setupViewPager() {
		stateAdapter = ViewPagerStateAdapter(this, images)
		binding.pager.apply {
			adapter = stateAdapter
			setPageTransformer(ZoomOutPageTransformer())
			setCurrentItem(position, false)
		}
	}

	private fun navigateUp() {
		BitmapCache.Instance.clear()
		onBackPressedDispatcher.onBackPressed()
	}

	private inner class PageSelectedCallback : ViewPager2.OnPageChangeCallback() {
		override fun onPageSelected(position: Int) {
			supportActionBar?.title = "${position + 1}/${images.size}"
		}
	}

	companion object {
		private const val MIME_TYPE = "image/*"
	}
}