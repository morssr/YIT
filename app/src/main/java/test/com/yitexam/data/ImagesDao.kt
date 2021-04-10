package test.com.yitexam.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImagesDao {

    @Query("SELECT * FROM images")
    fun getAllImagesPaging(): PagingSource<Int, Image>

    @Query("SELECT web_format_url FROM images")
    fun getAllImagesFlow(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<Image>)

    @Query("DELETE FROM images")
    suspend fun clearAll()
}