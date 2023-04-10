package io.github.xamdr.noties.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.domain.usecase.CreateTagUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagsViewModel @Inject constructor(private val createTagUseCase: CreateTagUseCase) : ViewModel() {

	fun createTag(tag: Tag) {
		viewModelScope.launch { createTagUseCase(tag) }
	}
}