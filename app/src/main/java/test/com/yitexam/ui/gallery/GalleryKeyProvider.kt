package test.com.yitexam.ui.gallery

import androidx.recyclerview.selection.ItemKeyProvider
import test.com.yitexam.data.Image

private const val TAG = "GalleryKeyProvider"

class GalleryKeyProvider(private val adapter: GalleryAdapter) : ItemKeyProvider<Image>(SCOPE_MAPPED) {
    override fun getKey(position: Int): Image? = adapter.getImageItem(position)
    override fun getPosition(key: Image): Int = adapter.getPosition(key.id)
}