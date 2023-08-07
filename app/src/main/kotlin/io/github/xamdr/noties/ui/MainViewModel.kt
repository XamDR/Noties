package io.github.xamdr.noties.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xamdr.noties.domain.model.Tag
import io.github.xamdr.noties.domain.usecase.GetTagsUseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val getTagsUseCase: GetTagsUseCase) : ViewModel() {

	fun getTags(): LiveData<List<Tag>> = getTagsUseCase().asLiveData()
}