package net.azurewebsites.eznotes.ui.audio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import net.azurewebsites.eznotes.core.MediaItemEntity
import net.azurewebsites.eznotes.databinding.AudioItemBinding
import net.azurewebsites.eznotes.ui.helpers.printDebug

class AudioAdapter(private val exoPlayer: ExoPlayer) : ListAdapter<MediaItemEntity, AudioAdapter.AudioViewHolder>(AudioItemAdapterCallback()) {

	inner class AudioViewHolder(private val binding: AudioItemBinding) : RecyclerView.ViewHolder(binding.root) {

		fun bind(mediaItemEntity: MediaItemEntity) {
			binding.apply {
				this.mediaItem = mediaItemEntity
				executePendingBindings()
				if (mediaItemEntity.uri != null) {
					exoPlayer.setMediaItem(MediaItem.fromUri(mediaItemEntity.uri))
				}
			}
		}

		fun prepare() {
			binding.audioPlayer.player = exoPlayer
			exoPlayer.prepare()
		}

		fun release() {
			binding.audioPlayer.player = null
			exoPlayer.release()
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
		val binding = AudioItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return AudioViewHolder(binding)
	}

	override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
		val audio = getItem(position)
		holder.bind(audio)
		printDebug("audio", audio)
	}

	override fun onViewAttachedToWindow(holder: AudioViewHolder) {
		super.onViewAttachedToWindow(holder)
		holder.prepare()
	}

	override fun onViewDetachedFromWindow(holder: AudioViewHolder) {
		super.onViewDetachedFromWindow(holder)
		holder.release()
	}

	private class AudioItemAdapterCallback : DiffUtil.ItemCallback<MediaItemEntity>() {

		override fun areItemsTheSame(oldItem: MediaItemEntity, newItem: MediaItemEntity) = oldItem.id == newItem.id

		override fun areContentsTheSame(oldItem: MediaItemEntity, newItem: MediaItemEntity) = oldItem == newItem
	}
}