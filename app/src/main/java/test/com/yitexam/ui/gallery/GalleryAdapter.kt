package test.com.yitexam.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.selection.SelectionTracker
import test.com.yitexam.data.Image
import test.com.yitexam.data.ImageDiffCallback
import test.com.yitexam.databinding.GalleryListItemBinding

private const val TAG = "GalleryAdapter"
const val SPAN_HEIGHT_FLEXIBLE_FLAG = -1

class GalleryAdapter(private val listener: ImagesAdapterListener) :
    PagingDataAdapter<Image, GalleryViewHolder>(ImageDiffCallback) {

    interface ImagesAdapterListener {
        fun onImageClicked(image: Image, position: Int)
    }

    var tracker: SelectionTracker<Image>? = null
    var spanSizeHeight = SPAN_HEIGHT_FLEXIBLE_FLAG

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        return GalleryViewHolder(
            GalleryListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            spanSizeHeight
        )
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        tracker?.let {
            holder.bind(getItem(position) as Image, listener, it.isSelected(getItem(position)))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun getImageItem(position: Int): Image? = getItem(position)
    fun getPosition(id: Long) = snapshot().indexOfFirst { it?.id == id }
}