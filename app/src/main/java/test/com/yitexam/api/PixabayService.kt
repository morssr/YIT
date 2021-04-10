package test.com.yitexam.api

import retrofit2.http.GET
import retrofit2.http.Query

//"https://pixabay.com/api/?q=kittens&key=6814610-cd083c066ad38bb511337fb2b"

interface PixabayService {
    @GET("?key=$API_KEY")
    suspend fun searchImages(
        @Query("q") query: String,
        @Query("image_type") imageType: String = SEARCH_IMAGE_TYPE,
        @Query("page") page: Int,
        @Query("per_page") itemsPerPage: Int = NETWORK_PAGE_SIZE
    ): PixabayImagesResponse
}
