package test.com.yitexam.ui.gallery

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import test.com.yitexam.data.Image

class GalleryItemDetailsLookup(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Image>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Image>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null && recyclerView.getChildViewHolder(view) is GalleryViewHolder) {
            return (recyclerView.getChildViewHolder(view) as GalleryViewHolder)
                .getItemDetails()
        }
        return null
    }
}