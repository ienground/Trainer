package net.ienlab.trainer.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ienlab.trainer.R
import net.ienlab.trainer.activity.SplashActivity
import net.ienlab.trainer.constant.NotificationChannelId
import net.ienlab.trainer.constant.NotificationId
import net.ienlab.trainer.room.TrainingDatabase
import net.ienlab.trainer.room.TrainingEntity
import java.text.DecimalFormat
import java.util.*

class AlarmReceiver: BroadcastReceiver() {
    private var trainingDatabase: TrainingDatabase? = null
    lateinit var nm: NotificationManager

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        trainingDatabase = TrainingDatabase.getInstance(context)
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

            nm.createNotificationChannel(NotificationChannel(NotificationChannelId.REMINDER, context.getString(R.string.reminder), NotificationManager.IMPORTANCE_DEFAULT))
            nm.notify(NotificationId.REMINDER, NotificationCompat.Builder(context, NotificationChannelId.REMINDER).apply {
                setContentTitle(context.getString(R.string.reminder_title))
                setContentText(context.getString(R.string.reminder_msg, String.format("%02d:%02d", runMin / 600, (runMin % 600) / 10),
                    numberFormat.format(pushupMax), numberFormat.format(situpMax)))
                setSmallIcon(R.drawable.ic_icon)
                setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, SplashActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                color = ContextCompat.getColor(context, R.color.colorAccent)
            }.build())
        }

    }
}