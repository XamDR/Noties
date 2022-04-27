package net.azurewebsites.eznotes.ui.media

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.print.PrintHelper
import androidx.viewpager2.widget.ViewPager2
import net.azurewebsites.eznotes.R
import net.azurewebsites.eznotes.core.MediaItemEntity
import net.azurewebsites.eznotes.databinding.ActivityGalleryBinding
import net.azurewebsites.eznotes.ui.helpers.printError
import net.azurewebsites.eznotes.ui.helpers.showToast
import net.azurewebsites.eznotes.ui.image.BitmapCache
import java.io.FileNotFoundException

class GalleryActivity : AppCompatActivity() {

	private lateinit var binding: ActivityGalleryBinding
	private lateinit var stateAdapter: ViewPagerStateAdapter
	private lateinit var mediaItems: List<MediaItemEntity>
	private var position: Int = 0
	private val callback = PageSelectedCallback()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityGalleryBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setSupportActionBar(binding.toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		mediaItems = intent.extras?.getParcelableArrayList("items") ?: emptyList()
		position = intent.getIntExtra("pos", 0)
		setupViewPager()
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
		R.id.print -> {
			printImage(binding.pager.currentItem); true
		}
		android.R.id.home -> {
			BitmapCache.Instance.clear()
			onBackPressed(); true
		}
		else -> super.onOptionsItemSelected(item)
	}

	private fun setupViewPager() {
		stateAdapter = ViewPagerStateAdapter(this, mediaItems)
		binding.pager.apply {
			adapter = stateAdapter
			setPageTransformer(ZoomOutPageTransformer())
			setCurrentItem(position, false)
		}
	}

	private fun shareImage(position: Int) {
		val shareIntent = Intent().apply {
			action = Intent.ACTION_SEND
			putExtra(Intent.EXTRA_STREAM, mediaItems[position].uri)
			type = "image/*"
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)))
	}

	private fun setImageAs(position: Int) {
		val intent = Intent().apply {
			action = Intent.ACTION_ATTACH_DATA
			setDataAndType(mediaItems[position].uri, "image/*")
			putExtra("mimeType", "image/*")
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		startActivity(Intent.createChooser(intent, getString(R.string.set_image_as)))
	}

	private fun printImage(position: Int) {
		val jobName = getString(R.string.print_image_job_name, position)
		val printHelper = PrintHelper(this).apply { scaleMode = PrintHelper.SCALE_MODE_FILL }
		try {
			showToast(R.string.init_print_dialog)
			val imageFile = mediaItems[position].uri ?: return
			printHelper.printBitmap(jobName, imageFile)
		}
		catch (e: FileNotFoundException) {
			printError("EXCEPTION", e.message)
			showToast(R.string.error_print_image)
		}
	}

	private inner class PageSelectedCallback : ViewPager2.OnPageChangeCallback() {
		override fun onPageSelected(position: Int) {
			supportActionBar?.title = "${position + 1}/${mediaItems.size}"
		}
	}
}