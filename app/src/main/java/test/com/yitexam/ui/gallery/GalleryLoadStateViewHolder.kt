package test.com.yitexam.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import test.com.yitexam.R
import test.com.yitexam.databinding.GalleryLoadStateFooterViewItemBinding

class GalleryLoadStateViewHolder(
    private val binding: GalleryLoadStateFooterViewItemBinding,
    retry: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retryButton.setOnClickListener { retry.invoke() }
    }

    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            binding.errorMsg.text = loadState.error.localizedMessage
        }
        binding.progressBar.isVisible = loadState is LoadState.Loading
        binding.retryButton.isVisible = loadState is LoadState.Error
        binding.errorMsg.isVisible = loadState is LoadState.Error
    }

    companion object {
        fun create(parent: ViewGroup, retry: () -> Unit): GalleryLoadStateViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.gallery_load_state_footer_view_item, parent, false)
            val binding = GalleryLoadStateFooterViewItemBinding.bind(view)
            return GalleryLoadStateViewHolder(binding, retry)
        }
    }
}
