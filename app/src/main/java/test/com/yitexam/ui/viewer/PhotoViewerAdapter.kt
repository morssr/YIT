package test.com.yitexam.ui.viewer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import test.com.yitexam.R
import test.com.yitexam.databinding.PhotoViewerContainerBinding

class PhotoViewerAdapter(private var photos: List<String> = listOf()) :
    RecyclerView.Adapter<PhotoViewerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemList = DataBindingUtil.inflate<PhotoViewerContainerBinding>(
            LayoutInflater.from(parent.context),
            R.layout.photo_viewer_container,
            parent,
            false
        )
        return ViewHolder(itemList)
    }

    override fun getItemCount(): Int = photos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    fun setPhotos(photos: List<String>) {
        this@PhotoViewerAdapter.photos = photos
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: PhotoViewerContainerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String) {
            with(binding) {
                val circularProgressDrawable = CircularProgressDrawable(binding.root.context)
                circularProgressDrawable.strokeWidth = 10f
                circularProgressDrawable.centerRadius = 60f
                circularProgressDrawable.start()

                Glide.with(root).load(imageUrl).placeholder(circularProgressDrawable)
                    .error(R.drawable.ic_error)
                    .into(imageView)
            }
        }
    }

    companion object {
        private const val TAG = "PhotoViewerAdapter"
    }
}