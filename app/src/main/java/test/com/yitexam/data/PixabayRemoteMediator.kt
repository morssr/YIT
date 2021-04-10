package test.com.yitexam.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import retrofit2.HttpException
import test.com.yitexam.api.PixabayService
import java.io.IOException

private const val TAG = "PixabayRemoteMediator"
private const val PIXBAY_STARTING_PAGE_INDEX = 1
const val LOAD_CASHED_DATA_FLAG = "-1"

@OptIn(ExperimentalPagingApi::class)
class PixbayRemoteMediator(
    private val query: String,
    private val service: PixabayService,
    private val appDatabase: AppDatabase
) : RemoteMediator<Int, Image>() {

    override suspend fun initialize(): InitializeAction {
        //skips initial refresh and loads cached data from room(sql)
        return if (query == LOAD_CASHED_DATA_FLAG)
            InitializeAction.SKIP_INITIAL_REFRESH
        //starts data loading process from remote source.
        else InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Image>): MediatorResult {
        Log.d(
            TAG,
            "load: type: $loadType || paging state anchor pos: ${state.anchorPosition} | page size: ${state.pages.size} | ${Thread.currentThread().name}"
        )
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                Log.d(TAG, "load: LoadType.REFRESH: remote key: $remoteKeys")
                remoteKeys?.nextKey?.minus(1) ?: PIXBAY_STARTING_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                // If the previous key is null, then the list is empty so we should wait for data
                // fetched by remote refresh and can simply skip loading this time by returning
                // `false` for endOfPaginationReached.
                val prevKey = remoteKeys?.prevKey
                if (prevKey == null) {
//                    Log.d(TAG, "load: prevKey == null")
                    Log.w(TAG, "load: LoadType.PREPEND: previous key is null, wait for refresh.")
                    return MediatorResult.Success(endOfPaginationReached = false)
                }
                Log.d(TAG, "load: previous key is not null | $remoteKeys")
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                // If the next key is null, then the list is empty so we should wait for data
                // fetched by remote refresh and can simply skip loading this time by returning
                // `false` for endOfPaginationReached.
                val nextKey = remoteKeys?.nextKey
                if (nextKey == null) {
                    Log.w(TAG, "load: LoadType.APPEND: next key is null, wait for refresh.")
                    return MediatorResult.Success(endOfPaginationReached = false)
                }
                Log.d(TAG, "load: next key is not null | $remoteKeys")
                nextKey
            }
        }
        Log.i(TAG, "load: next page to load: $page")

        try {
            val apiResponse = service.searchImages(query = query, page = page)

            val images = apiResponse.hits
            val endOfPaginationReached = images.isEmpty()
            appDatabase.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    appDatabase.remoteKeysDao().clearRemoteKeys()
                    appDatabase.imagesDao().clearAll()
                    Log.w(
                        TAG,
                        "load: clear all cashed data called | T: ${Thread.currentThread().name}"
                    )
                }
                val prevKey = if (page == PIXBAY_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = images.map {
                    RemoteKeys(0, imageId = it.imageId, prevKey = prevKey, nextKey = nextKey)
                }
                printRemoteKeys(keys)
                Log.d(TAG, "load: insert remote keys called | remote keys size: ${keys.size}")
                appDatabase.remoteKeysDao().insertAll(keys)
                Log.d(TAG, "load: insert repos called | repos size: ${images.size}")
                appDatabase.imagesDao().insertAll(images)
                Log.i(TAG, "load: load page '$page' accomplished")
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            Log.e(TAG, "load: load failed", exception)
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            Log.e(TAG, "load: load failed", exception)
            return MediatorResult.Error(exception)
        }

    }

    private fun printRemoteKeys(keys: List<RemoteKeys>) {
        keys.forEach {
            Log.v(TAG, "printRemoteKeys: $it")
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Image>): RemoteKeys? {
        Log.d(
            TAG,
            "getRemoteKeyForLastItem: paging state anchor pos: ${state.anchorPosition} | page size: ${state.pages.size}"
        )
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { image ->
                // Get the remote keys of the last item retrieved
                val keys = appDatabase.remoteKeysDao().remoteKeysImageId(image.imageId)
                Log.d(
                    TAG,
                    "getRemoteKeyForLastItem: the last key from the last page. | keys: $keys"
                )
                keys
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Image>): RemoteKeys? {
        Log.d(
            TAG,
            "getRemoteKeyForFirstItem: paging state anchor pos: ${state.anchorPosition} | page size: ${state.pages.size}"
        )
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { it ->
                // Get the remote keys of the first items retrieved
                val keys = appDatabase.remoteKeysDao().remoteKeysImageId(it.imageId)
                Log.d(
                    TAG,
                    "getRemoteKeyForFirstItem: the first key from the first page. | keys: $keys"
                )
                keys
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Image>
    ): RemoteKeys? {
        Log.d(
            TAG,
            "getRemoteKeyClosestToCurrentPosition: paging state anchor pos: ${state.anchorPosition} | page size: ${state.pages.size}"
        )
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.imageId?.let { imageId ->
                val keys = appDatabase.remoteKeysDao().remoteKeysImageId(imageId)
                Log.d(
                    TAG,
                    "getRemoteKeyClosestToCurrentPosition: the closest key from the current position. | keys: $keys"
                )
                keys
            }
        }
    }

}