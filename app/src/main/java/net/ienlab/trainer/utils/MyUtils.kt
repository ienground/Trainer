package net.ienlab.trainer.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import androidx.core.content.ContextCompat
import java.io.*
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import kotlin.math.abs

class MyUtils {
    companion object {
        fun getCurrentLocale(context: Context): Locale {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                context.resources.configuration.locales.get(0)
            else
                context.resources.configuration.locale
        }

        fun isAppInstalled(context: Context, packageName: String): Boolean {
            return try {
                context.packageManager.getApplicationInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        fun dpToPx(context: Context, dpValue: Float): Float {
            val metrics = context.resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics)
        }

        fun URL.toBitmap(): Bitmap?{
            return try {
                BitmapFactory.decodeStream(openStream())
            } catch (e: IOException){
                null
            }
        }

        fun Bitmap.saveToInternalStorage(context: Context, fileName: String): Uri? {
            val wrapper = ContextWrapper(context)
            val file = File(wrapper.getDir("images", Context.MODE_PRIVATE), "${fileName}.png")
            return try {
                // get the file output stream
                val stream: OutputStream = FileOutputStream(file)
                compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.flush()
                stream.close()

                // return the saved image uri
                Uri.parse(file.absolutePath)
            } catch (e: IOException){ // catch the exception
                e.printStackTrace()
                null
            }
        }
        
        fun getBitmapFromVectorDrawable(context: Context, drawableId: Int, width: Int, height: Int, color: Int): Bitmap {
            val drawable = ContextCompat.getDrawable(context, drawableId)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            }

            drawable?.setBounds(0, 0, canvas.width, canvas.height)
            drawable?.draw(canvas)

            val bitmapResult = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvasResult = Canvas(bitmapResult)
            canvasResult.drawBitmap(bitmap, 0f, 0f, paint)

            return bitmapResult
        }

        fun fromHtml(source: String): Spanned {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(source)
            }
        }

        fun readTextFromRaw(res: Resources, rawId: Int): String {
            val inputStream = res.openRawResource(rawId)
            val stream = InputStreamReader(inputStream, Charset.forName("UTF-8"))
            val buffer = BufferedReader(stream)
            val builder = StringBuilder()

            buffer.lineSequence().forEach { builder.append(it) }
            inputStream.close()

            return builder.toString()
        }

        fun comparedDate(c1: Calendar, c2: Calendar): Long {
            c1.apply {
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            c2.apply {
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            return c1.timeInMillis - c2.timeInMillis
        }

        fun comparedTime(c1: Calendar, c2: Calendar): Boolean {
            val now = Calendar.getInstance()
            val e1 = c1.clone() as Calendar
            val e2 = c2.clone() as Calendar

            with (e1) {
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            }

            with (e2) {
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            }

            return abs(e1.timeInMillis - e2.timeInMillis) <= 10 * 60 * 1000
        }

        fun Context.isNetworkConnected(): Boolean {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = cm.activeNetwork
                val networkCap = cm.getNetworkCapabilities(activeNetwork)

                (networkCap?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false || networkCap?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false || networkCap?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ?: false || networkCap?.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ?: false)
            } else {
                val networkInfo = cm.activeNetworkInfo
                networkInfo?.isConnected ?: false
            }
        }

        fun Calendar.timeZero(): Calendar {
            val calendar = this.clone() as Calendar
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            return calendar
        }

        fun Calendar.tomorrowZero(): Calendar {
            val calendar = this.clone() as Calendar
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            return calendar
        }

        fun getColor(context: Context, colorResId: Int): Int {
            val typedValue = TypedValue()
            val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
            val color = typedArray.getColor(0, 0)
            typedArray.recycle()
            return color
        }
    }
}