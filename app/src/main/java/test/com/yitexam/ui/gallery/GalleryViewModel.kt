package test.com.yitexam.ui.gallery

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import test.com.yitexam.data.Image
import test.com.yitexam.data.GalleryRepository
import javax.inject.Inject

private const val TAG = "GalleryViewModel"

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: GalleryRepository
) : ViewModel() {
    private var currentQueryValue: String? = null

    private var currentSearchResult: Flow<PagingData<Image>>? = null

    fun searchImages(queryString: String): Flow<PagingData<Image>> {
        Log.d(TAG, "searchImages: search images called with query string: $queryString")
        val lastResult = currentSearchResult
        if (queryString == currentQueryValue && lastResult != null) {
            return lastResult
        }
        val newResult: Flow<PagingData<Image>> = repository.getSearchResultStream(queryString)
            .cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }
}