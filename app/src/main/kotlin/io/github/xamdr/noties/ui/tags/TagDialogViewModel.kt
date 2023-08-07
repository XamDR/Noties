package io.github.xamdr.noties.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.domain.usecase.CreateTagUseCase
import io.github.xamdr.noties.domain.usecase.GetTagNamesUseCase
import io.github.xamdr.noties.domain.usecase.UpdateTagUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagDialogViewModel @Inject constructor(
	private val createTagUseCase: CreateTagUseCase,
	private val updateTagUseCase: UpdateTagUseCase,
	private val getTagNamesUseCase: GetTagNamesUseCase,) : ViewModel() {

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

	suspend fun createTag(tag: Tag) {
		createTagUseCase(tag)
		names.add(tag.name)
		clearNameState()
	}

	fun clearNameState() {
		tagNameState.update { TagNameState.EmptyOrUpdatingName }
	}

	suspend fun updateTag(tag: Tag, oldTag: Tag) {
		updateTagUseCase(tag)
		names.remove(oldTag.name)
		names.add(tag.name)
		clearNameState()
	}
}