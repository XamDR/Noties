package io.github.xamdr.noties.ui.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.xamdr.noties.R
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.media.MediaHelper
import io.github.xamdr.noties.ui.theme.NotiesTheme
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteList(
	modifier: Modifier,
	notes: List<Note>,
	onNoteClick: (Note) -> Unit,
	onNoteMovedToTrash: (Note) -> Unit
) {
	LazyColumn(
		modifier = modifier,
		contentPadding = PaddingValues(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		items(
			count = notes.size,
			key = { index -> notes[index].id.toInt() }
		) { index ->
			val currentNote by rememberUpdatedState(newValue = notes[index])
			val dismissState = rememberDismissState(confirmValueChange = { dissmissValue ->
				when (dissmissValue) {
					DismissValue.DismissedToEnd -> {
						Timber.d("Note archived"); true
					}
					DismissValue.DismissedToStart -> {
						onNoteMovedToTrash(currentNote); true
					}
					else -> false
				}
			})
			SwipeToDismiss(
				state = dismissState,
				background = {},
				dismissContent = { NoteItem(note = notes[index], onClick = onNoteClick) },
				modifier = Modifier.animateItemPlacement()
			)
		}
	}
}

@Composable
fun NoteList(modifier: Modifier) {
	val notes = listOf(
		Note(id = 0, title = "Lorem Ipsum", text = stringResource(id = R.string.demo_content)),
		Note(id = 1, title = "Lorem Ipsum", text = stringResource(id = R.string.demo_content)),
		Note(id = 2, title = "Lorem Ipsum", text = stringResource(id = R.string.demo_content)),
		Note(id = 3, title = "Lorem Ipsum", text = stringResource(id = R.string.demo_content)),
		Note(id = 4, title = "Lorem Ipsum", text = stringResource(id = R.string.demo_content)),
	)
	LazyColumn(
		modifier = modifier,
		contentPadding = PaddingValues(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		items(
			count = notes.size,
			key = { index -> notes[index].id.toInt() }
		) { NoteItem() }
	}
}

@DevicePreviews
@Composable
private fun NoteListPreview() {
	NotiesTheme {
		NoteList(Modifier)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteItem(note: Note, onClick: (note: Note) -> Unit) {
	OutlinedCard(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors(containerColor = Color.Transparent),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
		border = BorderStroke(width = 1.5.dp, color = MaterialTheme.colorScheme.surfaceVariant),
		onClick = { onClick(note) }
	) {
		Column {
			if (note.previewItem != null) {
				Box(
					modifier = Modifier.fillMaxWidth(),
					contentAlignment = Alignment.TopEnd
				) {
					AsyncImage(
						contentScale = ContentScale.Crop,
						model = ImageRequest.Builder(LocalContext.current)
							.data(
								if (note.previewItem?.mediaType == MediaType.Image) note.previewItem?.uri
								else note.previewItem?.metadata?.thumbnail ?: R.drawable.ic_image_not_supported
							)
							.build(),
						contentDescription = stringResource(id = R.string.user_generated_image)
					)
					if (note.previewItem?.mediaType == MediaType.Video) {
						Box(
							modifier = Modifier.padding(8.dp)
						) {
							Row(
								modifier = Modifier.background(
									color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
									shape = RoundedCornerShape(8.dp)
								)
							) {
								Icon(
									imageVector = Icons.Outlined.PlayCircle,
									contentDescription = null,
									modifier = Modifier.padding(4.dp)
								)
								Text(
									text = MediaHelper.formatDuration(note.previewItem?.metadata?.duration),
									style = MaterialTheme.typography.labelSmall,
									fontWeight = FontWeight.Bold,
									modifier = Modifier
										.align(Alignment.CenterVertically)
										.padding(end = 8.dp)
								)
							}
						}
					}
				}
			}
			if (note.title.isNotEmpty()) {
				Text(
					text = note.title,
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.Bold,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
				)
			}
			if (note.text.isNotEmpty()) {
				val topDp = if (note.title.isNotEmpty()) 8.dp else 16.dp
				Text(
					text = note.text,
					style = MaterialTheme.typography.bodyLarge,
					maxLines = 5,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.padding(start = 16.dp, top = topDp, end = 16.dp, bottom = 16.dp)
				)
			}
		}
	}
}

@Composable
private fun NoteItem() {
	OutlinedCard(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors(containerColor = Color.Transparent),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
		border = BorderStroke(width = 1.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
	) {
		Column {
			Box(
				modifier = Modifier.fillMaxWidth(),
				contentAlignment = Alignment.TopEnd
			) {
				Image(
					imageVector = Icons.Outlined.Android,
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxWidth()
				)
				Box(
					modifier = Modifier.padding(8.dp)
				) {
					Row(
						modifier = Modifier.background(
							color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
							shape = RoundedCornerShape(8.dp)
						)
					) {
						Icon(
							imageVector = Icons.Outlined.PlayCircle,
							contentDescription = null,
							modifier = Modifier.padding(4.dp)
						)
						Text(
							text = "04:40",
							style = MaterialTheme.typography.labelSmall,
							fontWeight = FontWeight.Bold,
							modifier = Modifier
								.align(Alignment.CenterVertically)
								.padding(end = 8.dp)
						)
					}
				}
			}
			Text(
				text = "Lorem Ipsum",
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Bold,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
			)
			Text(
				text = stringResource(id = R.string.demo_content),
				style = MaterialTheme.typography.bodyLarge,
				maxLines = 5,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
			)
		}
	}
}
