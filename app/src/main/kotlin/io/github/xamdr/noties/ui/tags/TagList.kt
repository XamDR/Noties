package io.github.xamdr.noties.ui.tags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.helpers.getDimensionFromAttrs
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun TagList(
	modifier: Modifier,
	tags: List<Tag>
) {
	LazyColumn(
		modifier = modifier,
		contentPadding = PaddingValues(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		items(
			count = tags.size,
			key = { index -> tags[index].id }
		) {
			index -> TagItem(tags[index])
		}
	}
}

@Composable
fun TagList(modifier: Modifier) {
	val tags = listOf(
		Tag(id = 0, name = "Android"),
		Tag(id = 1, name = "Kotlin"),
		Tag(id = 2, name = "React"),
		Tag(id = 3, name = "Work"),
		Tag(id = 4, name = "Private"),
		Tag(id = 5, name = "Pokimane"),
		Tag(id = 6, name = "Games"),
	)
	LazyColumn(
		modifier = modifier,
		contentPadding = PaddingValues(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		items(
			count = tags.size,
			key = { index -> tags[index].id }
		) { index -> TagItem(tags[index]) }
	}
}

@DevicePreviews
@Composable
private fun TagListPreview() = NotiesTheme { TagList(Modifier) }

@Composable
private fun TagItem(tag: Tag) {
	val context = LocalContext.current
	val startPadding = context.getDimensionFromAttrs(android.R.attr.listPreferredItemPaddingStart)
	val endPadding = context.getDimensionFromAttrs(android.R.attr.listPreferredItemPaddingEnd)
	val minHeight = context.getDimensionFromAttrs(android.R.attr.listPreferredItemHeight)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(start = startPadding.dp, top = 8.dp, end = endPadding.dp, bottom = 8.dp)
			.defaultMinSize(minHeight = minHeight.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.Outlined.Label,
			contentDescription = null,
			modifier = Modifier.padding(8.dp)
		)
		Text(
			text = tag.name,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			style = MaterialTheme.typography.bodyLarge,
			modifier = Modifier
				.weight(1f)
				.padding(top = 6.dp, end = 8.dp, bottom = 8.dp)
		)
		Checkbox(checked = false, onCheckedChange = {})
	}
}