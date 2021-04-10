package test.com.yitexam.utilities

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast

private const val TAG = "ShareUtils"

object ShareUtils {

    @JvmStatic
    fun shareMultipleImagesURLs(activity: Activity, selectedImagesUrl: List<String>?) {
        if (selectedImagesUrl == null || selectedImagesUrl.isNullOrEmpty()) {
            Log.e(TAG, "shareMultipleImagesURLs: failed to share images. Empty list.")
            Toast.makeText(activity, "No items selected.", Toast.LENGTH_SHORT).show()
            return
        }
        Intent().also { intent ->
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_SUBJECT, "Images from Pixbay")
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, buildShareableString(selectedImagesUrl))
            activity.startActivity(Intent.createChooser(intent, null))
        }
    }

    @JvmStatic
    private fun buildShareableString(images: List<String>): String {
        val stringBuilder = StringBuilder()
        images.forEachIndexed { index, s ->
            stringBuilder.appendLine(s)
            if (images.size > 1) stringBuilder.appendLine()
        }
        return stringBuilder.toString()
    }
}