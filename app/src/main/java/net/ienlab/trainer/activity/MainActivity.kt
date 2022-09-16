package net.ienlab.trainer.activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import net.ienlab.trainer.BuildConfig
import net.ienlab.trainer.R
import net.ienlab.trainer.constant.ApiKey
import net.ienlab.trainer.constant.IntentKey
import net.ienlab.trainer.constant.SharedKey
import net.ienlab.trainer.databinding.ActivityMainBinding
import net.ienlab.trainer.receiver.AlarmReceiver
import net.ienlab.trainer.room.TrainingDatabase
import net.ienlab.trainer.room.TrainingEntity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection
import kotlin.math.max
import kotlin.math.min


const val TAG = "TrainerTAG"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager
    lateinit var calendar: Calendar
    lateinit var dateSaveFormat: SimpleDateFormat
    lateinit var numberFormat: DecimalFormat
    lateinit var dateFormat: SimpleDateFormat

    private var trainingDatabase: TrainingDatabase? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        trainingDatabase = TrainingDatabase.getInstance(this)
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
        }
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerTime.timeInMillis, AlarmManager.INTERVAL_DAY,
            PendingIntent.getBroadcast(this, 0, Intent(this, AlarmReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

        // Weather
//        val header = "Bearer $accessToken"

        numberFormat = DecimalFormat("###,###")
        dateSaveFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        dateFormat = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        val startCalendar = Calendar.getInstance().apply {
            time = dateSaveFormat.parse(sharedPreferences.getInt(SharedKey.INPUT_DAY, 20200101).toString()) ?: Date()
        }
        val endCalendar = Calendar.getInstance().apply {
            time = dateSaveFormat.parse(sharedPreferences.getInt(SharedKey.OUTPUT_DAY, 20200101).toString()) ?: Date()
        }
        val goalCalendar = Calendar.getInstance().apply {
            time = dateSaveFormat.parse(sharedPreferences.getInt(SharedKey.GOAL_DAY, 20200101).toString()) ?: Date()
        }
        calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val milProgress = TimeUnit.DAYS.convert(calendar.timeInMillis - startCalendar.timeInMillis, TimeUnit.MILLISECONDS).toFloat()
        val milMax = TimeUnit.DAYS.convert(endCalendar.timeInMillis - startCalendar.timeInMillis, TimeUnit.MILLISECONDS).toFloat()

        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria().apply {
            accuracy = Criteria.ACCURACY_FINE
            isAltitudeRequired = false
            isBearingRequired = false
            isSpeedRequired = false
            isCostAllowed = true
            powerRequirement = Criteria.POWER_LOW
        }

        val locationProvider = lm.getBestProvider(criteria, true)
        val geocoder = Geocoder(this, Locale.getDefault())

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) { // TODO: Consider calling
            lm.requestLocationUpdates(locationProvider ?: LocationManager.GPS_PROVIDER, 1000, 100f) { location ->
                val accuracy = location.accuracy
                val lat = location.latitude
                val lng = location.longitude
                val city = geocoder.getFromLocation(lat, lng, 1)
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
                        val weather = JSONObject(response).getJSONArray("weather")
                        val main = JSONObject(response).getJSONObject("main")
                        Log.d(TAG, weather.toString())
                        Log.d(TAG, main.toString())
                        Log.d(TAG, main.getDouble("temp").toInt().toString())
                        withContext(Dispatchers.Main) {
                            binding.tvTemp.text = "${main.getDouble("temp").toInt()}°C"
                            binding.tvCity.text = city.first().adminArea
                            binding.tvDust.text = getString(R.string.humid_format, main.getInt("humidity"))
                            binding.ivWeather.setImageResource(when (weather.getJSONObject(0).getString("description")) {
                                "clear sky" -> R.drawable.ic_weather_sunny
                                "few clouds" -> R.drawable.ic_weather_few_clouds
                                "scattered clouds", "broken clouds" -> R.drawable.ic_weather_scatter_clouds
                                "shower rain", "rain" -> R.drawable.ic_weather_rain
                                "thunderstorm" -> R.drawable.ic_weather_thunder
                                "snow" -> R.drawable.ic_weather_snow
                                "mist" -> R.drawable.ic_weather_mist
                                else -> R.drawable.ic_weather_sunny
                            })

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        }

        val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                binding.tvDateGoal.text = sharedPreferences.getString(SharedKey.GOAL_LV, "0").let { if (it == "3") getString(R.string.special_level) else getString(R.string.nlevel, 3 - (it?.toInt() ?: 0)) }
                setData()
            }
        }

        val addActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setData()
            }
        }

        // user data on screen

        sharedPreferences.getString(SharedKey.PROFILE_URI, "").let {
            val profileImage = Uri.parse(it)
            if (it != "") binding.ivProfile.setImageURI(profileImage)
        }
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
        binding.btnSettings.setOnClickListener {
            settingsLauncher.launch(Intent(this, SettingsActivity::class.java))
        }
        binding.cardRunning.setOnClickListener {
            addActivityLauncher.launch(Intent(this, ViewRecordActivity::class.java).apply {
                putExtra(IntentKey.VIEW_RECORD_TYPE, TrainingEntity.TYPE_RUN)
            })
        }
        binding.cardPushup.setOnClickListener {
            addActivityLauncher.launch(Intent(this, ViewRecordActivity::class.java).apply {
                putExtra(IntentKey.VIEW_RECORD_TYPE, TrainingEntity.TYPE_PUSHUP)
            })
        }
        binding.cardSitup.setOnClickListener {
            addActivityLauncher.launch(Intent(this, ViewRecordActivity::class.java).apply {
                putExtra(IntentKey.VIEW_RECORD_TYPE, TrainingEntity.TYPE_SITUP)
            })
        }
        binding.tvDateGoal.text = sharedPreferences.getString(SharedKey.GOAL_LV, "3").let { if (it == "3") getString(R.string.special_level) else getString(R.string.nlevel, 3 - (it?.toInt() ?: 0)) }

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

        binding.addCardSitup.setOnClickListener {
            startActivity(Intent(this, SitupAddActivity::class.java))
            binding.groupAdd.visibility = View.GONE
        }

        sharedPreferences.getInt(SharedKey.GRADE, 10).let {
            when (it / 10) {
                1 -> binding.ivGrade.setImageResource(R.drawable.ic_lv_1)
                2 -> binding.ivGrade.setImageResource(R.drawable.ic_lv_2)
                3 -> binding.ivGrade.setImageResource(R.drawable.ic_lv_3)
                4 -> binding.ivGrade.setImageResource(R.drawable.ic_lv_4)
            }
        }

        setData()

        binding.ivProfile.setOnLongClickListener {
//            if (BuildConfig.DEBUG)
                startActivity(Intent(this, DataInputActivity::class.java))
            true
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setData() {
        GlobalScope.launch(Dispatchers.IO) {
            val entitiesRun = trainingDatabase?.getDao()?.get(TrainingEntity.TYPE_RUN, calendar.timeInMillis - AlarmManager.INTERVAL_DAY * 9, calendar.timeInMillis + AlarmManager.INTERVAL_DAY)
            val entitiesPushup = trainingDatabase?.getDao()?.get(TrainingEntity.TYPE_PUSHUP, calendar.timeInMillis - AlarmManager.INTERVAL_DAY * 9, calendar.timeInMillis + AlarmManager.INTERVAL_DAY)
            val entitiesSitup = trainingDatabase?.getDao()?.get(TrainingEntity.TYPE_SITUP, calendar.timeInMillis - AlarmManager.INTERVAL_DAY * 9, calendar.timeInMillis + AlarmManager.INTERVAL_DAY)
            val todayRun = trainingDatabase?.getDao()?.get(TrainingEntity.TYPE_RUN, calendar.timeInMillis, calendar.timeInMillis + AlarmManager.INTERVAL_DAY)
            val todayPushup = trainingDatabase?.getDao()?.get(TrainingEntity.TYPE_PUSHUP, calendar.timeInMillis, calendar.timeInMillis + AlarmManager.INTERVAL_DAY)
            val todaySitup = trainingDatabase?.getDao()?.get(TrainingEntity.TYPE_SITUP, calendar.timeInMillis, calendar.timeInMillis + AlarmManager.INTERVAL_DAY)
            val goalLv = sharedPreferences.getString(SharedKey.GOAL_LV, "3")?.toInt() ?: 0
            val goalRun = sharedPreferences.getInt(if (goalLv == 3) SharedKey.RUN_LVS else "run_lv_${3 - goalLv}", 0) * 10
            val goalPushup = sharedPreferences.getInt(if (goalLv == 3) SharedKey.PUSHUP_LVS else "pushup_lv_${3 - goalLv}", 0)
            val goalSitup = sharedPreferences.getInt(if (goalLv == 3) SharedKey.SITUP_LVS else "situp_lv_${3 - goalLv}", 0)
            var runSum = 0
            var pushupSum = 0
            var situpSum = 0
            var todayRunMin = Int.MAX_VALUE
            var todayPushupMax = -1
            var todaySitupMax = -1

            if (entitiesRun != null) {
                for (entity in entitiesRun) {
                    runSum += entity.count
                }
            }
            if (entitiesPushup != null) {
                for (entity in entitiesPushup) {
                    pushupSum += entity.count
                }
            }
            if (entitiesSitup != null) {
                for (entity in entitiesSitup) {
                    situpSum += entity.count
                }
            }
            if (todayRun != null) {
                for (entity in todayRun) {
                    if (todayRunMin > entity.count) todayRunMin = entity.count
                }
            }
            if (todayPushup != null) {
                for (entity in todayPushup) {
                    if (todayPushupMax < entity.count) todayPushupMax = entity.count
                }
            }
            if (todaySitup != null) {
                for (entity in todaySitup) {
                    if (todaySitupMax < entity.count) todaySitupMax = entity.count
                }
            }

            val progressRun = entitiesRun?.let { if (it.isNotEmpty()) (runSum / it.size) else 0 }?.toFloat() ?: 0f
            val progressPushup = entitiesPushup?.let { if (it.isNotEmpty()) (pushupSum / it.size) else 0 }?.toFloat() ?: 0f
            val progressSitup = entitiesSitup?.let { if (it.isNotEmpty()) (situpSum / it.size) else 0 }?.toFloat() ?: 0f

            var todayRunLevel = 4
            var todayPushupLevel = 4
            var todaySitupLevel = 4
            var currentRunLevel = 4
            var currentPushupLevel = 4
            var currentSitupLevel = 4

            val cutlineRun = listOf(sharedPreferences.getInt(SharedKey.RUN_LVS, 0), sharedPreferences.getInt(SharedKey.RUN_LV1, 0), sharedPreferences.getInt(SharedKey.RUN_LV2, 0), sharedPreferences.getInt(SharedKey.RUN_LV3, 0))
            val cutlinePushup = listOf(sharedPreferences.getInt(SharedKey.PUSHUP_LVS, 0), sharedPreferences.getInt(SharedKey.PUSHUP_LV1, 0), sharedPreferences.getInt(SharedKey.PUSHUP_LV2, 0), sharedPreferences.getInt(SharedKey.PUSHUP_LV3, 0))
            val cutlineSitup = listOf(sharedPreferences.getInt(SharedKey.SITUP_LVS, 0), sharedPreferences.getInt(SharedKey.SITUP_LV1, 0), sharedPreferences.getInt(SharedKey.SITUP_LV2, 0), sharedPreferences.getInt(SharedKey.SITUP_LV3, 0))

            cutlineRun.reversed().forEachIndexed { index, i ->
                if (progressRun <= i * 10) {
                    currentRunLevel = 3 - index
                }
                if (todayRunMin <= i * 10) {
                    todayRunLevel = 3 - index
                }
                Log.d(TAG, "$progressRun $todayRunMin $i")
            }

            cutlinePushup.reversed().forEachIndexed { index, i ->
                if (progressPushup >= i) {
                    currentPushupLevel = 3 - index
                }
                if (todayPushupMax >= i) {
                    todayPushupLevel = 3 - index
                }
            }

            Log.d(TAG, "$progressPushup $todayPushupMax")

            cutlineSitup.reversed().forEachIndexed { index, i ->
                if (progressSitup >= i) {
                    currentSitupLevel = 3 - index
                }
                if (todaySitupMax >= i) {
                    todaySitupLevel = 3 - index
                }
            }

            Log.d(TAG, "$currentRunLevel $currentPushupLevel $currentSitupLevel ${max(max(currentRunLevel, currentPushupLevel), currentSitupLevel)}")
            Log.d(TAG, "$todayRunLevel $todayPushupLevel $todaySitupLevel ${max(max(todayRunLevel, todayPushupLevel), todaySitupLevel)}")

            val todayLevel = max(max(todayRunLevel, todayPushupLevel), todaySitupLevel)
            var currentLevel = max(max(currentRunLevel, currentPushupLevel), currentSitupLevel)

            if (goalLv >= todayLevel) { // smaller -> greater. over success
                sharedPreferences.edit().putInt(SharedKey.SUCCESS_DATE, dateSaveFormat.format(calendar.time).toInt()).apply()
                sharedPreferences.edit().putInt(SharedKey.SUCCESS_LV, todayLevel).apply()
                currentLevel = todayLevel
                Log.d(TAG, "S1")
            } else {
                val successDate = Calendar.getInstance().apply {
                    time = dateSaveFormat.parse(sharedPreferences.getInt(SharedKey.SUCCESS_DATE, 20201131).toString())
                }
                Log.d(TAG, "S2")
                if (calendar.get(Calendar.DAY_OF_YEAR) - successDate.get(Calendar.DAY_OF_YEAR) >= 7) {
                    currentLevel = sharedPreferences.getInt(SharedKey.SUCCESS_LV, 4)
                    Log.d(TAG, "S3")
                }
            }
            //            else if (progressRun > goalRun || progressPushup < goalPushup || progressSitup < goalSitup) {
            //
            //            }

            withContext(Dispatchers.Main) {
                binding.progressRun.max = goalRun.toFloat()
                binding.progressPushup.max = goalPushup.toFloat()
                binding.progressSitup.max = goalSitup.toFloat()
                binding.progressRun.progress = (2f * goalRun - progressRun).let { if (it <= 0 || progressRun == 0f) 0f else it }
                binding.progressPushup.progress = progressPushup
                binding.progressSitup.progress = progressSitup
                binding.tvPercentRun.text = if (progressRun != 0f) String.format("%02d:%02d / %02d:%02d", progressRun.toInt() / 600, (progressRun.toInt() % 600) / 10, goalRun / 600, (goalRun % 600) / 10) else String.format("- / %02d:%02d", goalRun / 600, (goalRun % 600) / 10)
                binding.tvPercentPushup.text = "${if (progressPushup != 0f) numberFormat.format(progressPushup) else "-"} / ${numberFormat.format(goalPushup)}"
                binding.tvPercentSitup.text = "${if (progressSitup != 0f) numberFormat.format(progressSitup) else "-"} / ${numberFormat.format(goalSitup)}"
                binding.tvDateCurrent.text = currentLevel.let { if (it == 0) getString(R.string.special_level) else if (it == 4) getString(R.string.failed) else getString(R.string.nlevel, it) }
            }
        }
    }
}