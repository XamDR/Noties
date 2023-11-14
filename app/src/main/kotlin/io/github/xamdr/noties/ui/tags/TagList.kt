package io.github.xamdr.noties.ui.tags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.ui.helpers.DevicePreviews
import io.github.xamdr.noties.ui.theme.NotiesTheme

@Composable
fun TagList(
	tags: List<Tag>,
	selectedTags: List<String>,
	onTagSelected: (Boolean, String) -> Unit,
	modifier: Modifier
) {
	LazyColumn(
		modifier = modifier,
		contentPadding = PaddingValues(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {

		items(
			count = tags.size,
			key = { index -> tags[index].id }
		) { index ->
			TagItem(
				checked = selectedTags.contains(tags[index].name),
				tag = tags[index],
				onTagSelected = onTagSelected
			)
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
		) { index ->
			TagItem(
				checked = false,
				tag = tags[index],
				onTagSelected = { _, _ -> }
			)
		}
	}
}

@DevicePreviews
@Composable
private fun TagListPreview() = NotiesTheme { TagList(Modifier) }

@Composable
private fun TagItem(
	checked: Boolean,
	tag: Tag,
	onTagSelected: (Boolean, String) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(all = 8.dp),
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
		Checkbox(
			checked = checked,
			onCheckedChange = { checked -> onTagSelected(checked, tag.name) }
		)
	}
}