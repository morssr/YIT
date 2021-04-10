package test.com.yitexam.ui.gallery

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.StaggeredGridLayoutManager


class GalleryLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<GalleryLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: GalleryLoadStateViewHolder, loadState: LoadState) {
        (holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): GalleryLoadStateViewHolder {
        return GalleryLoadStateViewHolder.create(parent, retry)
    }
}
