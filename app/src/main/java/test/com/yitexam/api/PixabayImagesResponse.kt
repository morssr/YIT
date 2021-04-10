package test.com.yitexam.api

import test.com.yitexam.data.Image

data class PixabayImagesResponse(
    val total: Int,
    val totalHits: Int,
    val hits: List<Image>
)