package net.azurewebsites.noties.ui.gallery

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.print.PrintHelper
import androidx.viewpager2.widget.ViewPager2
import net.azurewebsites.noties.R
import net.azurewebsites.noties.core.ImageEntity
import net.azurewebsites.noties.databinding.ActivityGalleryBinding
import net.azurewebsites.noties.ui.helpers.printError
import net.azurewebsites.noties.ui.helpers.showToast
import java.io.FileNotFoundException

class GalleryActivity : AppCompatActivity(), GalleryMenuListener {

	private lateinit var binding: ActivityGalleryBinding
	private lateinit var stateAdapter: ViewPagerStateAdapter
	private val images by lazy(LazyThreadSafetyMode.NONE) {
		intent.extras?.getParcelableArrayList<ImageEntity>(IMAGES) ?: emptyList()
	}
	private val position by lazy(LazyThreadSafetyMode.NONE) {
		intent.getIntExtra(POSITION, 0)
	}
	private val callback = PageSelectedCallback()
	private val menuProvider = GalleryMenuProvider(this)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityGalleryBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		setupViewPager()
		addMenuProvider(menuProvider, this)
	}

	override fun onStart() {
		super.onStart()
		callback.onPageSelected(position)
	}

	override fun onPause() {
		super.onPause()
		binding.pager.unregisterOnPageChangeCallback(callback)
	}

	override fun onResume() {
		super.onResume()
		binding.pager.registerOnPageChangeCallback(callback)
	}

	override fun shareImage(position: Int) {
		val shareIntent = Intent().apply {
			action = Intent.ACTION_SEND
			putExtra(Intent.EXTRA_STREAM, images[position].uri)
			type = MIME_TYPE
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)))
	}

	override fun setImageAs(position: Int) {
		val intent = Intent().apply {
			action = Intent.ACTION_ATTACH_DATA
			setDataAndType(images[position].uri, MIME_TYPE)
			putExtra("mimeType", MIME_TYPE)
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		startActivity(Intent.createChooser(intent, getString(R.string.set_image_as)))
	}

	override fun printImage(position: Int) {
		val jobName = getString(R.string.print_image_job_name, position)
		val printHelper = PrintHelper(this).apply { scaleMode = PrintHelper.SCALE_MODE_FILL }
		try {
			showToast(R.string.init_print_dialog)
			val imageFile = images[position].uri ?: return
			printHelper.printBitmap(jobName, imageFile)
		}
		catch (e: FileNotFoundException) {
			printError(TAG, e.message)
			showToast(R.string.error_print_image)
		}
	}

	override val currentItem: Int
		get() = binding.pager.currentItem

	private fun setupViewPager() {
		stateAdapter = ViewPagerStateAdapter(this, images)
		binding.pager.apply {
			adapter = stateAdapter
			setPageTransformer(ZoomOutPageTransformer())
			setCurrentItem(position, false)
		}
	}

	private inner class PageSelectedCallback : ViewPager2.OnPageChangeCallback() {
		override fun onPageSelected(position: Int) {
			supportActionBar?.title = "${position + 1}/${images.size}"
		}
	}

	companion object {
		const val IMAGES = "images"
		const val POSITION = "position"
		private const val MIME_TYPE = "image/*"
		private const val TAG = "PRINT_ERROR"
	}
}