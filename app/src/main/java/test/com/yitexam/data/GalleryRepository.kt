package test.com.yitexam.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import test.com.yitexam.api.NETWORK_PAGE_SIZE
import test.com.yitexam.api.PixabayService
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GalleryRepository"

@Singleton
class GalleryRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val pixabayService: PixabayService
) {
    fun getSearchResultStream(query: String): Flow<PagingData<Image>> {
        Log.d(TAG, "New query: $query")

        val pagingSourceFactory = { appDatabase.imagesDao().getAllImagesPaging() }

        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            remoteMediator = PixbayRemoteMediator(
                query,
                pixabayService,
                appDatabase
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    fun getAllCashedImagesFlow(): Flow<List<String>> = appDatabase.imagesDao().getAllImagesFlow()
}
