package net.ienlab.trainer.activity

import android.app.AlarmManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.icu.number.NumberFormatter
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.*
import net.ienlab.trainer.R
import net.ienlab.trainer.adapter.ViewRecordAdapter
import net.ienlab.trainer.constant.IntentKey
import net.ienlab.trainer.constant.SharedKey
import net.ienlab.trainer.databinding.ActivityViewRecordBinding
import net.ienlab.trainer.room.TrainingDatabase
import net.ienlab.trainer.room.TrainingEntity
import net.ienlab.trainer.utils.MyUtils
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ViewRecordActivity : AppCompatActivity() {

    private var trainingDatabase: TrainingDatabase? = null

    lateinit var binding: ActivityViewRecordBinding
    lateinit var sharedPreferences: SharedPreferences

    @OptIn(DelicateCoroutinesApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_record)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        trainingDatabase = TrainingDatabase.getInstance(this)
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        binding.graph.setNoDataTextColor(ContextCompat.getColor(this, R.color.black))
        binding.graph.setNoDataText(getString(R.string.no_datas))
        binding.graph.setNoDataTextTypeface(ResourcesCompat.getFont(this, R.font.pretendard_regular) ?: Typeface.DEFAULT)
        binding.bottomNav.setOnItemSelectedListener { menu ->
            val type = when (menu.itemId) {
                R.id.navigation_run -> TrainingEntity.TYPE_RUN
                R.id.navigation_pushup -> TrainingEntity.TYPE_PUSHUP
                R.id.navigation_situp -> TrainingEntity.TYPE_SITUP
                else -> TrainingEntity.TYPE_RUN
            }

            GlobalScope.launch(Dispatchers.IO) {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val entitiesToday = trainingDatabase?.getDao()?.get(type, calendar.timeInMillis, calendar.timeInMillis + AlarmManager.INTERVAL_DAY)
                val entities10 = trainingDatabase?.getDao()?.get(type, calendar.timeInMillis - AlarmManager.INTERVAL_DAY * 9, calendar.timeInMillis + AlarmManager.INTERVAL_DAY)
                val entities = trainingDatabase?.getDao()?.get(type)?.sortedByDescending { it.timestamp }
                var entities10Sum = 0
                var entitiesTodayValue = 0
                var entitiesMax = Int.MIN_VALUE
                var entitiesMin = Int.MAX_VALUE
                var dayMax = Long.MIN_VALUE
                var dayMin = Long.MAX_VALUE

                withContext(Dispatchers.Main) {
                    binding.recyclerView.adapter = ViewRecordAdapter(entities ?: listOf())
                    val goalLv = sharedPreferences.getString(SharedKey.GOAL_LV, "3")?.toInt() ?: 0
                    val goal = when (type) {
                        TrainingEntity.TYPE_RUN -> sharedPreferences.getInt(if (goalLv == 3) SharedKey.RUN_LVS else "run_lv_${3 - goalLv}", 0) * 10
                        TrainingEntity.TYPE_PUSHUP -> sharedPreferences.getInt(if (goalLv == 3) SharedKey.PUSHUP_LVS else "pushup_lv_${3 - goalLv}", 0)
                        TrainingEntity.TYPE_SITUP -> sharedPreferences.getInt(if (goalLv == 3) SharedKey.SITUP_LVS else "situp_lv_${3 - goalLv}", 0)
                        else -> sharedPreferences.getInt(if (goalLv == 3) SharedKey.RUN_LVS else "run_lv_${3 - goalLv}", 0)
                    }

                    val entries = arrayListOf<Entry>()

                    if (entities10 != null) {
                        for (entity in entities10) {
                            val time = Calendar.getInstance().apply {
                                timeInMillis = entity.timestamp
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }
                            entities10Sum += entity.count
                            if (entitiesMax < entity.count) entitiesMax = entity.count
                            if (entitiesMin > entity.count) entitiesMin = entity.count
                            if (dayMax < time.timeInMillis) dayMax = time.timeInMillis
                            if (dayMin > time.timeInMillis) dayMin = time.timeInMillis
                            entries.add(Entry(time.timeInMillis.toFloat(), entity.count.toFloat()))
                        }
                    }

                    val data = LineDataSet(entries, "label")
                    val lineData = LineData()
                    val dateFormat = SimpleDateFormat(getString(R.string.dateAxisFormat), Locale.getDefault())
                    val numberFormat = DecimalFormat("###,###")

                    data.setCircleColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
                    data.lineWidth = 3f
                    data.circleHoleRadius = 30f
                    data.valueTextSize = 12f
                    data.color = ContextCompat.getColor(applicationContext, R.color.colorPrimary)

                    data.setValueFormatter { value, entry, dataSetIndex, viewPortHandler ->
                        if (type == TrainingEntity.TYPE_RUN) String.format("%02d:%02d", value.toInt() / 600, (value.toInt() % 600) / 10)
                        else numberFormat.format(value)
                    }
                    lineData.addDataSet(data)
                    lineData.setValueTextColor(ContextCompat.getColor(applicationContext, R.color.black))

                    binding.graph.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    binding.graph.xAxis.setValueFormatter { value, axis ->
                        dateFormat.format(value.toLong())
                    }
                    binding.graph.axisRight.axisMinimum = entitiesMin.toFloat().let { if (it - 5 > 0) it - 5 else 0f }
                    binding.graph.axisRight.axisMaximum = entitiesMax.toFloat() + 5
                    binding.graph.xAxis.axisMinimum = dayMin.toFloat() - AlarmManager.INTERVAL_DAY
                    binding.graph.xAxis.axisMaximum = dayMax.toFloat() + AlarmManager.INTERVAL_DAY
                    binding.graph.description.isEnabled = false

                    binding.graph.xAxis.textColor = ContextCompat.getColor(applicationContext, R.color.black)
                    binding.graph.xAxis.setDrawGridLines(false)
                    binding.graph.axisLeft.setDrawLabels(false)
                    binding.graph.axisRight.setDrawLabels(false)
                    binding.graph.axisLeft.setDrawGridLines(false)
                    binding.graph.axisRight.setDrawGridLines(false)

                    if (entries.isNotEmpty()) binding.graph.data = lineData
                    binding.graph.legend.isEnabled = false
                    binding.graph.invalidate()

                    if (type != TrainingEntity.TYPE_RUN) {
                        binding.todayProgress.max = goal.toFloat()
                        binding.avgProgress.max = goal.toFloat()
                        binding.todayProgress.progress = entitiesToday?.let { if (it.isNotEmpty()) it.last().count else 0 }?.toFloat() ?: 0f
                        binding.avgProgress.progress = entities10?.let { if (it.isNotEmpty()) (entities10Sum / it.size) else 0 }?.toFloat() ?: 0f

                        binding.tvTodayProgress.text = numberFormat.format(entitiesToday?.let { if (it.isNotEmpty()) it.last().count else 0 })
                        binding.tvAvgProgress.text = numberFormat.format(entities10?.let { if (it.isNotEmpty()) (entities10Sum / it.size) else 0 })
                        binding.tvTodayMax.text = numberFormat.format(goal)
                        binding.tvAvgMax.text = numberFormat.format(goal)
                    } else {
                        val todayProgress = entitiesToday?.let { if (it.isNotEmpty()) it.last().count else 2 * goal }?.toFloat() ?: 0f
                        val avgProgress = entities10?.let { if (it.isNotEmpty()) (entities10Sum / it.size) else 0 }?.toFloat() ?: 0f

                        binding.todayProgress.max = goal.toFloat()
                        binding.avgProgress.max = goal.toFloat()
                        binding.todayProgress.progress = (2f * goal - todayProgress).let { if (it <= 0) 0f else it }
                        binding.avgProgress.progress = (2f * goal - avgProgress).let { if (it <= 0) 0f else it }

                        binding.tvTodayProgress.text = entitiesToday?.let { (if (it.isNotEmpty()) it.last().count else 0).let { String.format("%02d:%02d", it / 600, (it % 600) / 10) } }
                        binding.tvAvgProgress.text = entities10?.let { (if (it.isNotEmpty()) (entities10Sum / it.size) else 0).let { String.format("%02d:%02d", it / 600, (it % 600) / 10) } }
                        binding.tvTodayMax.text = String.format("%02d:%02d", goal / 600, (goal % 600) / 10)
                        binding.tvAvgMax.text = String.format("%02d:%02d", goal / 600, (goal  % 600) / 10)
                    }

                    if (entitiesToday?.isEmpty() == true) binding.tvTodayProgress.text = "-"
                    if (entities10?.isEmpty() == true) binding.tvAvgProgress.text = "-"

                    binding.checkTodayRecord.visibility = if (binding.todayProgress.progress >= binding.todayProgress.max) View.VISIBLE else View.GONE
                    binding.checkAvgRecord.visibility = if (binding.avgProgress.progress >= binding.avgProgress.max) View.VISIBLE else View.GONE

                }
            }

            true
        }

        binding.bottomNav.selectedItemId = when (intent?.getIntExtra(IntentKey.VIEW_RECORD_TYPE, TrainingEntity.TYPE_RUN)) {
            TrainingEntity.TYPE_RUN -> R.id.navigation_run
            TrainingEntity.TYPE_PUSHUP -> R.id.navigation_pushup
            TrainingEntity.TYPE_SITUP -> R.id.navigation_situp
            else -> R.id.navigation_run
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}