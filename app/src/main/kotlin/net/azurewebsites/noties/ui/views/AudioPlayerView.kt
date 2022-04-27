package net.azurewebsites.noties.ui.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import net.azurewebsites.noties.R
import net.azurewebsites.noties.databinding.AudioPlayerViewBinding

class AudioPlayerView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

	private lateinit var binding: AudioPlayerViewBinding
	private lateinit var player: ExoPlayer
	var src: Uri? = null

	init {
		init()
	}

	private fun init() {
		if (isInEditMode) return
		binding = AudioPlayerViewBinding.inflate(LayoutInflater.from(context), this, true)
		player = ExoPlayer.Builder(context).build()
		binding.playPause.setOnClickListener { toggleAudio() }
		binding.progress.addOnChangeListener { _, value, fromUser ->
			if (fromUser) {
				player.seekTo(value.toLong())
			}
		}
	}

	fun cleanUp() {
		player.release()
	}

	fun prepare() {
		val item = src?.let { MediaItem.fromUri(it) }
		if (item != null) {
			player.setMediaItem(item)
			player.prepare()
			player.addListener(object : Player.Listener {
				override fun onPlaybackStateChanged(state: Int) {
					if (state == Player.STATE_READY) {
						binding.duration.text = (player.duration / 1000).toString()
						binding.position.text = (player.currentPosition / 1000).toString()
					}
				}
			})
		}
	}

	private fun toggleAudio() {
		if (player.isPlaying) {
			pauseAudio()
			binding.playPause.setImageResource(R.drawable.ic_play)
		}
		else {
			playAudio()
			binding.playPause.setImageResource(R.drawable.ic_pause)
		}
	}

	private fun playAudio() {
		player.play()
		handler.postDelayed({
			binding.progress.value = (player.currentPosition / player.duration).toFloat()
			binding.position.text = (player.currentPosition / 1000).toString()
		}, 1000L)
	}

	private fun pauseAudio() {
		player.pause()
	}
}