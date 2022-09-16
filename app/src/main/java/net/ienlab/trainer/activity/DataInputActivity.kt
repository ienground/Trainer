package net.ienlab.trainer.activity

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ienlab.trainer.R
import net.ienlab.trainer.constant.NotificationChannelId
import net.ienlab.trainer.constant.NotificationId
import net.ienlab.trainer.databinding.ActivityDataInputBinding
import net.ienlab.trainer.room.TrainingDatabase
import net.ienlab.trainer.room.TrainingEntity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class DataInputActivity : AppCompatActivity() {

    lateinit var binding: ActivityDataInputBinding
    private var trainingDatabase: TrainingDatabase? = null

    @OptIn(DelicateCoroutinesApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_data_input)
        binding.activity = this

        trainingDatabase = TrainingDatabase.getInstance(this)

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        binding.tvDate.text = dateFormat.format(calendar.time)
        binding.tvTime.text = timeFormat.format(calendar.time)

        binding.tvDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvDate.text = dateFormat.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.tvTime.setOnClickListener {
            TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                binding.tvTime.text = timeFormat.format(calendar.time)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        binding.btnSave.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                trainingDatabase?.getDao()?.add(TrainingEntity(calendar.timeInMillis,
                    when (binding.groupRadio.checkedRadioButtonId) {
                        R.id.radio_run -> TrainingEntity.TYPE_RUN
                        R.id.radio_pushup -> TrainingEntity.TYPE_PUSHUP
                        R.id.radio_situp -> TrainingEntity.TYPE_SITUP
                        else -> TrainingEntity.TYPE_RUN
                    }
                    , binding.etCount.text.toString().toInt()))
            }
        }

        binding.btnNoti.setOnClickListener {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val numberFormat = DecimalFormat("###,###")
            GlobalScope.launch(Dispatchers.IO) {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                val runEntities = trainingDatabase?.getDao()?.get(TrainingEntity.TYPE_RUN, calendar.timeInMillis - AlarmManager.INTERVAL_DAY, calendar.timeInMillis)
                val pushupEntities = trainingDatabase?.getDao()?.get(TrainingEntity.TYPE_PUSHUP, calendar.timeInMillis - AlarmManager.INTERVAL_DAY, calendar.timeInMillis)
                val situpEntities = trainingDatabase?.getDao()?.get(TrainingEntity.TYPE_SITUP, calendar.timeInMillis - AlarmManager.INTERVAL_DAY, calendar.timeInMillis)
                var runMin = Int.MAX_VALUE
                var pushupMax = Int.MIN_VALUE
                var situpMax = Int.MIN_VALUE

                if (runEntities != null) {
                    for (entity in runEntities) {
                        if (runMin > entity.count) {
                            runMin = entity.count
                        }
                    }
                }

                if (pushupEntities != null) {
                    for (entity in pushupEntities) {
                        if (pushupMax < entity.count) {
                            pushupMax = entity.count
                        }
                    }
                }

                if (situpEntities != null) {
                    for (entity in situpEntities) {
                        if (situpMax < entity.count) {
                            situpMax = entity.count
                        }
                    }
                }

                val contentText = if (runMin != Int.MAX_VALUE && pushupMax != Int.MIN_VALUE && situpMax != Int.MIN_VALUE) {
                    getString(R.string.reminder_msg, String.format("%02d:%02d", runMin / 600, (runMin % 600) / 10),
                        numberFormat.format(pushupMax), numberFormat.format(situpMax))
                } else if (runMin != Int.MAX_VALUE && pushupMax != Int.MIN_VALUE) {
                    getString(R.string.reminder_msg_run_push, String.format("%02d:%02d", runMin / 600, (runMin % 600) / 10),
                        numberFormat.format(pushupMax))
                } else if (runMin != Int.MAX_VALUE && situpMax != Int.MIN_VALUE) {
                    getString(R.string.reminder_msg_run_sit, String.format("%02d:%02d", runMin / 600, (runMin % 600) / 10),
                        numberFormat.format(situpMax))
                } else if (pushupMax != Int.MIN_VALUE && situpMax != Int.MIN_VALUE) {
                    getString(R.string.reminder_msg_push_sit, numberFormat.format(pushupMax), numberFormat.format(situpMax))
                } else if (runMin != Int.MAX_VALUE) {
                    getString(R.string.reminder_msg_run, String.format("%02d:%02d", runMin / 600, (runMin % 600) / 10))
                } else if (pushupMax != Int.MIN_VALUE) {
                    getString(R.string.reminder_msg_push, numberFormat.format(pushupMax))
                } else if (situpMax != Int.MIN_VALUE) {
                    getString(R.string.reminder_msg_sit, numberFormat.format(situpMax))
                } else {
                    getString(R.string.reminder_msg_none)
                }

                nm.createNotificationChannel(NotificationChannel(NotificationChannelId.REMINDER, getString(R.string.reminder), NotificationManager.IMPORTANCE_DEFAULT))
                nm.notify(NotificationId.REMINDER, NotificationCompat.Builder(applicationContext, NotificationChannelId.REMINDER).apply {
                    setContentTitle(getString(R.string.reminder_title))
                    setContentText(contentText)
                    setSmallIcon(R.drawable.ic_icon)
                    setContentIntent(PendingIntent.getActivity(applicationContext, 0, Intent(applicationContext, SplashActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                    color = ContextCompat.getColor(applicationContext, R.color.colorAccent)
                }.build())
            }
        }

        binding.btnSampleData.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_MONTH, -1)
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_RUN, 10 * (15 * 60 + 35)))
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_PUSHUP, 48))
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_SITUP, 62))
                c.add(Calendar.DAY_OF_MONTH, -1)
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_RUN, 10 * (15 * 60 + 40)))
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_PUSHUP, 48))
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_SITUP, 60))
                c.add(Calendar.DAY_OF_MONTH, -1)
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_RUN, 10 * (15 * 60 + 49)))
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_PUSHUP, 46))
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_SITUP, 59))
                c.add(Calendar.DAY_OF_MONTH, -1)
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_RUN, 10 * (15 * 60 + 58)))
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_PUSHUP, 45))
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_SITUP, 59))
                c.add(Calendar.DAY_OF_MONTH, -1)
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_RUN, 10 * (16 * 60 + 3)))
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_PUSHUP, 43))
                trainingDatabase?.getDao()?.add(TrainingEntity(c.timeInMillis, TrainingEntity.TYPE_SITUP, 58))
                c.add(Calendar.DAY_OF_MONTH, -1)

            }
        }
    }
}