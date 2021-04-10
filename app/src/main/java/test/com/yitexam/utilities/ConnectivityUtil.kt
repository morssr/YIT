package test.com.yitexam.utilities

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.util.Log
import androidx.appcompat.app.AlertDialog
import test.com.yitexam.R
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object ConnectivityUtil {

    private const val TAG = "ConnectivityUtil"

    private const val GOOGLE_SERVER = "https://www.google.com"

    @JvmStatic
    @SuppressLint("MissingPermission")
    fun hasNetworkAvailable(context: Context): Boolean {
        val service = Context.CONNECTIVITY_SERVICE
        val manager = context.getSystemService(service) as ConnectivityManager?
        val network = manager?.activeNetworkInfo
        Log.d(TAG, "hasNetworkAvailable: ${(network != null)}")
        return (network?.isConnected) ?: false
    }

    @JvmStatic
    fun hasInternetConnected(context: Context): Boolean {
        if (hasNetworkAvailable(context)) {
            try {
                val connection = URL(GOOGLE_SERVER).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Test")
                connection.setRequestProperty("Connection", "close")
                connection.connectTimeout = 1500
                connection.connect()
                Log.d(TAG, "hasInternetConnected: ${(connection.responseCode == 200)}")
                return (connection.responseCode == 200)
            } catch (e: IOException) {
                Log.e(TAG, "Error checking internet connection", e)
            }
        } else {
            Log.w(TAG, "No network available!")
        }
        Log.d(TAG, "hasInternetConnected: false")
        return false
    }

    @JvmStatic
    fun hasServerConnected(context: Context): Boolean {
        if (hasNetworkAvailable(context)) {
            try {
                val connection = URL(GOOGLE_SERVER).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Test")
                connection.setRequestProperty("Connection", "close")
                connection.connectTimeout = 1500
                connection.connect()
                Log.d(TAG, "hasServerConnected: ${(connection.responseCode == 200)}")
                return (connection.responseCode == 200)
            } catch (e: IOException) {
                Log.e(TAG, "Error checking internet connection", e)
            }
        } else {
            Log.w(TAG, "Server is unavailable!")
        }
        Log.d(TAG, "hasServerConnected: false")
        return false
    }

    @JvmStatic
    fun showInternetConnectionLost(context: Context) {
        try {
            val dialog = AlertDialog.Builder(context).create()

            dialog.setTitle(context.getString(R.string.no_network_title))
            dialog.setMessage(context.getString(R.string.no_network_content))
            dialog.setCancelable(true)
            dialog.setButton(
                DialogInterface.BUTTON_POSITIVE,
                context.getString(android.R.string.ok)
            ) { _, _ -> dialog.dismiss() }
            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }
}