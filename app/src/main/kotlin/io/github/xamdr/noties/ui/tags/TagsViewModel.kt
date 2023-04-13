package io.github.xamdr.noties.ui.tags

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(
	private val createTagUseCase: CreateTagUseCase,
	private val getTagsUseCase: GetTagsUseCase,
	private val getTagNamesUseCase: GetTagNamesUseCase,
	private val updateTagUseCase: UpdateTagUseCase,
	private val deleteTagsUseCase: DeleteTagsUseCase) : ViewModel() {

	private val tagNameState: MutableStateFlow<TagNameState> = MutableStateFlow(TagNameState.EmptyOrUpdatingName)
	val nameState = tagNameState.asLiveData()

	private val names = mutableSetOf<String>()

	init {
		viewModelScope.launch {
			names.addAll(getTagNamesUseCase())
		}
	}

	fun onTagNameChanged(tagName: String) {
		if (tagName.isEmpty()) {
			tagNameState.update { TagNameState.EmptyOrUpdatingName }
		}
		else {
			if (names.contains(tagName)) {
				tagNameState.update { TagNameState.ErrorDuplicateName }
			}
			else {
				tagNameState.update { TagNameState.EditingName }
			}
		}
	}

	fun createTag(tag: Tag) {
		viewModelScope.launch {
			createTagUseCase(tag)
			names.add(tag.name)
			clearNameState()
		}
	}

	fun clearNameState() {
		tagNameState.update { TagNameState.EmptyOrUpdatingName }
	}

	fun getTags(): LiveData<List<Tag>> = getTagsUseCase()

	fun updateTag(tag: Tag, oldTag: Tag) {
		viewModelScope.launch {
			updateTagUseCase(tag)
			names.remove(oldTag.name)
			names.add(tag.name)
			clearNameState()
		}
	}

	fun deleteTags(tags: List<Tag>) {
		viewModelScope.launch {
			deleteTagsUseCase(tags)
			names.removeAll(tags.map { it.name }.toSet())
		}
	}
}