package net.ienlab.trainer.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
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
import net.ienlab.trainer.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection


const val TAG = "TrainerTAG"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        // Weather
//        val header = "Bearer $accessToken"
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
    }
}