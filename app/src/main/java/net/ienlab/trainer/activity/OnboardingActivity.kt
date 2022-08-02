package net.ienlab.trainer.activity

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import com.google.android.gms.common.api.Api
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ienlab.trainer.R
import net.ienlab.trainer.adapter.OnboardingPageAdapter
import net.ienlab.trainer.adapter.OnboardingPageAdapter.Companion.PAGE_NUMBER
import net.ienlab.trainer.constant.ApiKey
import net.ienlab.trainer.constant.SharedKey
import net.ienlab.trainer.databinding.ActivityOnboardingBinding
import net.ienlab.trainer.fragment.*
import net.ienlab.trainer.room.TrainingDatabase //import net.ienlab.blogplanner.room.PostPlanDatabase
import net.ienlab.trainer.room.TrainingEntity
import net.ienlab.trainer.utils.MyUtils
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
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
    private var trainingDatabase: TrainingDatabase? = null
    val cutlineMap: MutableMap<String, Triple<MutableList<Int>, MutableList<Int>, MutableList<Int>>> = mutableMapOf()
    val cutlineAge: MutableList<Int> = mutableListOf()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)
        binding.activity = this

        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        trainingDatabase = TrainingDatabase.getInstance(this)

        // 데이터 초기화
        sharedPreferences.edit().clear().apply()
        sharedPreferences.edit().putInt(SharedKey.GOAL_LV, 3).apply()
        deleteDatabase("TrainingData.db")
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
            if (sharedPreferences.getString(SharedKey.NICKNAME, "") != "" && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                sharedPreferences.edit().putBoolean(SharedKey.IS_FIRST_VISIT, false).apply()
                GlobalScope.launch(Dispatchers.IO) {
                    sharedPreferences.getInt(SharedKey.TEMP_RUN, -1).let {
                        if (it != -1) trainingDatabase?.getDao()?.add(TrainingEntity(System.currentTimeMillis(), TrainingEntity.TYPE_RUN, it))
                    }
                    sharedPreferences.getInt(SharedKey.TEMP_PUSHUP, -1).let {
                        if (it != -1) trainingDatabase?.getDao()?.add(TrainingEntity(System.currentTimeMillis(), TrainingEntity.TYPE_PUSHUP, it))
                    }
                    sharedPreferences.getInt(SharedKey.TEMP_SITUP, -1).let {
                        if (it != -1) trainingDatabase?.getDao()?.add(TrainingEntity(System.currentTimeMillis(), TrainingEntity.TYPE_SITUP, it))
                    }
                }
                finish()
                startActivity(Intent(this, MainActivity::class.java))
            } else if (sharedPreferences.getString(SharedKey.NICKNAME, "") == "") {
                Snackbar.make(window.decorView.rootView, getString(R.string.err_name_not_filled), Snackbar.LENGTH_SHORT).show()
            } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(window.decorView.rootView, getString(R.string.permission_granted_msg), Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(window.decorView.rootView, getString(R.string.err_name_and_permission), Snackbar.LENGTH_SHORT).show()
            }

            getCutline()
        }

        getCutline()

    }
    
    @OptIn(DelicateCoroutinesApi::class) 
    private fun getCutline() {
        val url = URL("https://openapi.mnd.go.kr/${ApiKey.MND_KEY}/json/DS_MND_MILPRSN_PHSTR_OFAPRV/1/233/")
    
        GlobalScope.launch(Dispatchers.IO) {
            val con = (url.openConnection() as HttpsURLConnection).apply {
                requestMethod = "GET"
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
//            Log.d(TAG, response)
            try {
                val jObject = JSONObject(response)//.getJSONObject("response")
                val data = jObject.getJSONObject("DS_MND_MILPRSN_PHSTR_OFAPRV").getJSONArray("row")
                Log.d(TAG, data.length().toString())
                for (i in 0 until data.length()) {
                    val obj = data.getJSONObject(i)
                    val ageUpper = obj.getString("age_uprlmtprcdc").let { if (it != "") it.toInt() else -1 }
                    val ageLower = obj.getString("age_lwlmtprcdc").let { if (it != "") it.toInt() else -1 }
                    val grade = obj.getString("grd")
                    val type = obj.getString("kind")
                    var typeCode = -1
                    var standardLower = -1
                    var standardUpper = -1

                    if (ageLower == -1 || ageUpper == -1) continue

                    Log.d(TAG, type.toString())
                    when (type) {
                        "3Km달리기" -> {
                            val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
                            standardUpper = obj.getString("std_uprlmtprcdc").let {
                                if (it != "") {
                                    val time = Calendar.getInstance().apply { time = timeFormat.parse(it) }
                                    time.get(Calendar.MINUTE) * 60 + time.get(Calendar.SECOND)
                                } else -1
                            }
                            standardLower = obj.getString("std_lwlmtprcdc").let {
                                if (it != "") {
                                    val time = Calendar.getInstance().apply { time = timeFormat.parse(it) }
                                    time.get(Calendar.MINUTE) * 60 + time.get(Calendar.SECOND)
                                } else -1
                            }
                            typeCode = TrainingEntity.TYPE_RUN
                        }

                        "팔굽혀펴기(2분)" -> {
                            standardUpper = obj.getString("std_uprlmtprcdc").let { if (it != "") it.toInt() else -1 }
                            standardLower = obj.getString("std_lwlmtprcdc").let { if (it != "") it.toInt() else -1 }
                            typeCode = TrainingEntity.TYPE_PUSHUP
                        }

                        "윗몸일으키기(2분)" -> {
                            standardUpper = obj.getString("std_uprlmtprcdc").let { if (it != "") it.toInt() else -1 }
                            standardLower = obj.getString("std_lwlmtprcdc").let { if (it != "") it.toInt() else -1 }
                            typeCode = TrainingEntity.TYPE_SITUP
                        }
                    }
                    if (!cutlineAge.contains(ageLower)) {
                        cutlineAge.add(ageLower)
                    }

                    val key = "${ageLower}_${ageUpper}"
                    if (!cutlineMap.containsKey(key)) {
                        cutlineMap[key] = Triple(mutableListOf(), mutableListOf(), mutableListOf())
                    }

                    var value = cutlineMap[key]
                    when (typeCode) {
                        TrainingEntity.TYPE_RUN -> value?.first?.add(standardLower)
                        TrainingEntity.TYPE_SITUP -> value?.second?.add(standardLower)
                        TrainingEntity.TYPE_PUSHUP -> value?.third?.add(standardLower)
                    }
                }

                cutlineAge.sort()

                Log.d(TAG, cutlineAge.toString())

                cutlineMap.forEach { (s, triple) ->
                    Log.d(TAG, "${s}, ${triple}")
                }

                // 차례대로 확인하며 Map(검색용)과 List(나이확인용) 완성

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