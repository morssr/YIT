package test.com.yitexam.data

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Entity(tableName = "images")
@Parcelize
data class Image(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @Transient
    val id: Long = 0,
    @Json(name = "id")
    @ColumnInfo(name = "image_id")
    val imageId: Long,
    @Json(name = "previewURL")
    @ColumnInfo(name = "preview_url")
    val previewUrl: String,
    @Json(name = "previewWidth")
    @ColumnInfo(name = "preview_width")
    val previewWidth: Int,
    @Json(name = "previewHeight")
    @ColumnInfo(name = "preview_height")
    val previewHeight: Int,
    @ColumnInfo(name = "web_format_url")
    @Json(name = "webformatURL")
    val webFormatUrl: String,
    @Json(name = "webformatWidth")
    @ColumnInfo(name = "web_format_width")
    val webFormatWidth: Int,
    @Json(name = "webformatHeight")
    @ColumnInfo(name = "web_format_height")
    val webFormatHeight: Int,
    @Json(name = "largeImageURL")
    @ColumnInfo(name = "full_size_url")
    val fullSizeUrl: String,
    @Json(name = "imageWidth")
    @ColumnInfo(name = "full_size_width")
    val fullSizeWidth: Int,
    @Json(name = "imageHeight")
    @ColumnInfo(name = "full_size_height")
    val fullSizeHeight: Int
) : Parcelable

object ImageDiffCallback : DiffUtil.ItemCallback<Image>() {
    override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean = oldItem == newItem
}