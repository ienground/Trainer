package net.ienlab.trainer.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ienlab.trainer.R
import net.ienlab.trainer.constant.ApiKey
import net.ienlab.trainer.constant.SharedKey
import net.ienlab.trainer.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection


const val TAG = "TrainerTAG"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var sharedPreferences: SharedPreferences

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        // Weather
//        val header = "Bearer $accessToken"

        val dateSaveFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dateFormat = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        val startCalendar = Calendar.getInstance().apply {
            time = dateSaveFormat.parse(sharedPreferences.getInt(SharedKey.INPUT_DAY, 20200101).toString())
        }
        val endCalendar = Calendar.getInstance().apply {
            time = dateSaveFormat.parse(sharedPreferences.getInt(SharedKey.OUTPUT_DAY, 20200101).toString())
        }
        val goalCalendar = Calendar.getInstance().apply {
            time = dateSaveFormat.parse(sharedPreferences.getInt(SharedKey.GOAL_DAY, 20200101).toString())
        }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val milProgress = TimeUnit.DAYS.convert(calendar.timeInMillis - startCalendar.timeInMillis, TimeUnit.MILLISECONDS).toFloat()
        val milMax = TimeUnit.DAYS.convert(endCalendar.timeInMillis - startCalendar.timeInMillis, TimeUnit.MILLISECONDS).toFloat()

        val lat = 37.81
        val lng = 127.11
        val apiURL = URL("https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lng}&appid=${ApiKey.OPENWEATHER_KEY}&units=metric")

        GlobalScope.launch(Dispatchers.IO) {
            val con = (apiURL.openConnection() as HttpsURLConnection).apply {
                requestMethod = "GET"
//                setRequestProperty("Authorization", header)
                connectTimeout = 15000
            }
            con.connect()
            val responseCode = con.responseCode

            val br = if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                BufferedReader(InputStreamReader(con.inputStream))//, 2100000000)
            } else {  // 에러 발생
                BufferedReader(InputStreamReader(con.errorStream))//, 2100000000)
            }

            val response = br.readLine(); br.close()
            Log.d(TAG, response)
            try {
                val jObject = JSONObject(response).getJSONObject("response")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        // user data on screen
        val profileImage = Uri.parse(sharedPreferences.getString(SharedKey.PROFILE_URI, ""))
        binding.ivProfile.setImageURI(profileImage)
        binding.tvNickname.text = sharedPreferences.getString(SharedKey.NICKNAME, "")
        binding.tvStart.text = dateFormat.format(startCalendar.time)
        binding.tvEnd.text = dateFormat.format(endCalendar.time)
        binding.millProgress.max = milMax
        binding.millProgress.progress = milProgress
        binding.millDay.text = "D-${(milMax - milProgress).toInt()}"
        binding.millPercent.text = "${milProgress * 100 / milMax}%"
        binding.btnAdd.setOnClickListener {
            binding.groupAdd.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        binding.groupAdd.visibility = View.VISIBLE
                    }
                })
        }

        val closeClickListener = View.OnClickListener {
            binding.groupAdd.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.groupAdd.visibility = View.GONE
                    }
                })
        }

        binding.btnClose.setOnClickListener(closeClickListener)
//        binding.blurAdd.setOnClickListener(closeClickListener)

        binding.addCardRunning.setOnClickListener {
            startActivity(Intent(this, RunAddActivity::class.java))
            binding.groupAdd.visibility = View.GONE
        }

        binding.addCardPushup.setOnClickListener {
            startActivity(Intent(this, PushupAddActivity::class.java))
            binding.groupAdd.visibility = View.GONE
        }
    }
}