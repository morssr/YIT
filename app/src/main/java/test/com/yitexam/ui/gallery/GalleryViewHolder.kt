package test.com.yitexam.ui.gallery

import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import test.com.yitexam.data.Image
import test.com.yitexam.databinding.GalleryListItemBinding

private const val TAG = "GalleryViewHolder"

class GalleryViewHolder(
    private val binding: GalleryListItemBinding,
    private val imageSpanHeightDp: Int = SPAN_HEIGHT_FLEXIBLE_FLAG
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        image: Image,
        listener: GalleryAdapter.ImagesAdapterListener,
        isActivated: Boolean = false
    ) {
        binding.image = image
        if (imageSpanHeightDp != SPAN_HEIGHT_FLEXIBLE_FLAG)
            binding.imageView.layoutParams.height = imageSpanHeightDp
        binding.card.setOnClickListener { listener.onImageClicked(image, absoluteAdapterPosition) }
        itemView.isActivated = isActivated
        binding.card.isChecked = isActivated

    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Image> =
        object : ItemDetailsLookup.ItemDetails<Image>() {
            override fun getPosition(): Int = layoutPosition
            override fun getSelectionKey(): Image? = binding.image
        }
}