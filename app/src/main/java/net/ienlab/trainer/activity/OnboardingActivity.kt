package net.ienlab.trainer.activity

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ienlab.trainer.R
import net.ienlab.trainer.adapter.OnboardingPageAdapter
import net.ienlab.trainer.adapter.OnboardingPageAdapter.Companion.PAGE_NUMBER
import net.ienlab.trainer.constant.SharedKey
import net.ienlab.trainer.databinding.ActivityOnboardingBinding
import net.ienlab.trainer.fragment.*
//import net.ienlab.blogplanner.room.PostPlanDatabase
import net.ienlab.trainer.utils.MyUtils
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.system.exitProcess

class OnboardingActivity : AppCompatActivity(),
    OnboardingFragment0.OnFragmentInteractionListener,
    OnboardingFragment1.OnFragmentInteractionListener,
    OnboardingFragment2.OnFragmentInteractionListener,
    OnboardingFragment3.OnFragmentInteractionListener,
    OnboardingFragment4.OnFragmentInteractionListener,
    OnboardingFragment5.OnFragmentInteractionListener {

    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    var page = 0

    lateinit var binding: ActivityOnboardingBinding

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)
        binding.activity = this

        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)

        // 데이터 초기화
        sharedPreferences.edit().clear().apply()

        var prePosition = -1

        binding.viewPager.setPageTransformer(MarginPageTransformer(MyUtils.dpToPx(this, 16f).toInt()))
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = OnboardingPageAdapter(this)
        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) {
                if (position == PAGE_NUMBER - 1) {
                    binding.btnFine.visibility = View.VISIBLE
                    ValueAnimator.ofFloat(0f, 1f).apply {
                        duration = 300
                        addUpdateListener {
                            binding.btnFine.alpha = (it.animatedValue as Float)
                        }
                    }.start()
                } else if (prePosition == PAGE_NUMBER - 1) {
                    ValueAnimator.ofFloat(1f, 0f).apply {
                        duration = 300
                        addUpdateListener {
                            binding.btnFine.alpha = (it.animatedValue as Float)
                            if (it.animatedValue as Float == 0f) {
                                binding.btnFine.visibility = View.GONE
                            }
                        }
                    }.start()
                } else {
                    binding.btnFine.visibility = View.GONE
                }

                prePosition = position
            }
        })
        binding.btnFine.setOnClickListener {
//            sharedPreferences.edit().putBoolean(SharedKey.IS_FIRST_VISIT, false).apply()
//            finish()
//            startActivity(Intent(this, MainActivity::class.java))
//            getCutline()
        }

    }
    
    @OptIn(DelicateCoroutinesApi::class) 
    private fun getCutline() {
        val url = URL("https://openapi.mnd.go.kr/3832313639343638353132353532313335/json/DS_MND_MILPRSN_PHSTR_OFAPRV/1/233/")
    
        GlobalScope.launch(Dispatchers.IO) {
            val con = (url.openConnection() as HttpsURLConnection).apply {
                requestMethod = "GET"
                //                setRequestProperty("Authorization", header)
                connectTimeout = 15000
            }
            con.connect()
            val responseCode = con.responseCode

            val br = if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                BufferedReader(InputStreamReader(con.inputStream)) //, 2100000000)
            } else {  // 에러 발생
                BufferedReader(InputStreamReader(con.errorStream)) //, 2100000000)
            }

            val response = br.readLine(); br.close()
            Log.d(TAG, response)
            try {
                val jObject = JSONObject(response).getJSONObject("response")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onFragmentInteraction(uri: Uri) {}

    override fun onBackPressed() {
        val tempTime = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime
        if (intervalTime in 0..FINISH_INTERVAL_TIME) {
            finishAffinity()
            System.runFinalization()
            exitProcess(0)
        } else {
            backPressedTime = tempTime
//            Toast.makeText(applicationContext, R.string.press_back_to_exit, Toast.LENGTH_SHORT).show()
        }
    }
}