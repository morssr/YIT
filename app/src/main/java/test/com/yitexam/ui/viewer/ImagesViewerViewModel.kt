package test.com.yitexam.ui.viewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import test.com.yitexam.data.GalleryRepository
import javax.inject.Inject

@HiltViewModel
class ImagesViewerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: GalleryRepository
) : ViewModel() {

    fun getAllCashedImagesFlow() = repository.getAllCashedImagesFlow()
}