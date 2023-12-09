package io.github.xamdr.noties.ui.notes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.xamdr.noties.R
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.DateTimeHelper
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.makeBulletedList
import io.github.xamdr.noties.ui.helpers.media.MediaHelper
import io.github.xamdr.noties.ui.theme.NotiesTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteList(
	modifier: Modifier,
	notes: List<Note>,
	selectedIds: MutableList<Long>,
	inSelectionMode: Boolean,
	onNoteClick: (Note) -> Unit,
	onNoteMovedToTrash: (Note) -> Unit,
	onNoteArchived: (Note) -> Unit
) {
	fun onClick(note: Note, selected: Boolean) {
		if (inSelectionMode) {
			var tempSelected = selected
			tempSelected = !tempSelected
			if (tempSelected) {
				selectedIds.add(note.id)
			}
			else {
				selectedIds.remove(note.id)
			}
		}
		else {
			onNoteClick(note)
		}
	}

	LazyColumn(
		modifier = modifier,
		contentPadding = PaddingValues(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		items(
			items = notes,
			key = { note -> note.id.toInt() }
		) { note ->
			val selected = selectedIds.contains(note.id)
			val currentNote by rememberUpdatedState(newValue = note)
			val dismissState = rememberDismissState(confirmValueChange = { dissmissValue ->
				when (dissmissValue) {
					DismissValue.DismissedToEnd -> {
						onNoteArchived(currentNote); true
					}
					DismissValue.DismissedToStart -> {
						onNoteMovedToTrash(currentNote); true
					}
					else -> false
				}
			})
			SwipeToDismiss(
				state = dismissState,
				background = { DismissBackground(dismissState) },
				dismissContent = {
					NoteItem(
						note = note,
						selected = selected,
						onClick = { note -> onClick(note, selected) },
						onLongClick = { selectedIds.add(note.id) }
					)
				},
				modifier = Modifier.animateItemPlacement()
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissBackground(state: DismissState) {
	val direction = state.dismissDirection ?: return
	val alignment = when (direction) {
		DismissDirection.StartToEnd -> Alignment.CenterStart
		DismissDirection.EndToStart -> Alignment.CenterEnd
	}
	val icon = when(state.targetValue) {
		DismissValue.Default -> null
		DismissValue.DismissedToEnd -> Icons.Outlined.Archive
		DismissValue.DismissedToStart -> Icons.Outlined.Delete
	}
	val scale by animateFloatAsState(
		targetValue = if (state.targetValue == DismissValue.Default) 0.75f else 1f,
		label = "scale"
	)
	val text = when(direction) {
		DismissDirection.StartToEnd -> stringResource(id = R.string.archive_note)
		DismissDirection.EndToStart -> stringResource(id = R.string.delete_note)
	}
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(color = Color.Transparent, shape = RoundedCornerShape(16.dp))
			.padding(horizontal = 16.dp),
		contentAlignment = alignment
	) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			if (icon != null) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					modifier = Modifier
						.size(32.dp)
						.scale(scale)
				)
				Text(text = text)
			}
		}
	}
}

@Composable
private fun DismissBackground() {
	Box(
		modifier = Modifier
			.background(color = Color.Transparent, shape = RoundedCornerShape(16.dp))
			.padding(horizontal = 16.dp),
		contentAlignment = Alignment.CenterEnd
	) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = Icons.Outlined.Delete,
				contentDescription = null,
				modifier = Modifier.size(32.dp)
			)
			Text(text = "Delete")
		}
	}
}

@DevicePreviews
@Composable
private fun DismissBackgroundPreview() = NotiesTheme { DismissBackground() }

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
			items = notes,
			key = { note -> note.id.toInt() },
			itemContent = { NoteItem() }
		)
	}
}

@DevicePreviews
@Composable
private fun NoteListPreview() = NotiesTheme { NoteList(Modifier) }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteItem(
	note: Note,
	selected: Boolean,
	onClick: (note: Note) -> Unit,
	onLongClick: () -> Unit
) {
	OutlinedCard(
		shape = RoundedCornerShape(16.dp),
		colors = CardDefaults.cardColors(containerColor = Color.Transparent),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
		border = BorderStroke(
			width = 1.5.dp,
			color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
		),
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(16.dp))
			.combinedClickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = rememberRipple(),
				onClickLabel = stringResource(id = R.string.navigate_editor),
				onClick = { onClick(note) },
				onLongClickLabel = stringResource(id = R.string.enter_multiselection_mode),
				onLongClick = onLongClick
			)
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
						contentDescription = stringResource(id = R.string.user_generated_image),
						modifier = Modifier.fillMaxWidth()
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
				if (note.isTaskList) {
					Text(
						text = makeBulletedList(input = note.text),
						style = MaterialTheme.typography.bodyLarge,
						maxLines = 5,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier.padding(start = 16.dp, top = topDp, end = 16.dp, bottom = 16.dp)
					)
				}
				else {
					Text(
						text = note.text,
						style = MaterialTheme.typography.bodyLarge,
						maxLines = 5,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier.padding(start = 16.dp, top = topDp, end = 16.dp, bottom = 16.dp)
					)
				}
			}
			if (note.tags.isNotEmpty() || note.reminderDate != null) {
				val allTags = if (note.reminderDate == null) note.tags
					else listOf(DateTimeHelper.formatDateTime(note.reminderDate)) + note.tags
				TagList(tags = allTags)
			}
		}
	}
}

@Composable
private fun NoteItem() {
	OutlinedCard(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(16.dp),
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
			TagList(tags = listOf("Android", "Work", "Personal"))
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagList(tags: List<String>) {
	FlowRow(
		modifier = Modifier
			.fillMaxWidth()
			.padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp),
		horizontalArrangement = Arrangement.Start
	) {
		tags.forEach { tag ->
			if (DateTimeHelper.isValidDate(tag)) {
				AssistChip(
					onClick = {},
					label = {
						Text(
							text = tag,
							textDecoration = if (DateTimeHelper.isPast(tag)) TextDecoration.LineThrough else null
						)
					},
					leadingIcon = {
						Icon(
							imageVector = Icons.Outlined.Alarm,
							contentDescription = null,
							modifier = Modifier.size(AssistChipDefaults.IconSize)
						)
					},
					modifier = Modifier.padding(horizontal = 4.dp)
				)
			}
			else {
				SuggestionChip(
					onClick = {},
					label = { Text(text = tag) },
					modifier = Modifier.padding(horizontal = 4.dp)
				)
			}
		}
	}
}