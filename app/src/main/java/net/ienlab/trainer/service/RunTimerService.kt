package net.ienlab.trainer.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.DelicateCoroutinesApi
import net.ienlab.trainer.R
import net.ienlab.trainer.activity.TAG
import net.ienlab.trainer.constant.IntentKey
import net.ienlab.trainer.constant.IntentValue
import net.ienlab.trainer.constant.NotificationChannelId
import net.ienlab.trainer.constant.NotificationId
import java.util.*

class RunTimerService : Service() {

    private var timerStart = false
    private var second = 0
    private var timer = Timer()
    private var lastLat = -1.0
    private var lastLng = -1.0
    private var distance = 0.0

    lateinit var nm: NotificationManager
    lateinit var lm: LocationManager
    private var locationProvider: String? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreste")

        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(NotificationChannelId.TIMER, getString(R.string.timer), NotificationManager.IMPORTANCE_DEFAULT))

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria().apply {
            accuracy = Criteria.ACCURACY_FINE
            isAltitudeRequired = false
            isBearingRequired = false
            isSpeedRequired = false
            isCostAllowed = true
            powerRequirement = Criteria.POWER_LOW
        }

//        locationProvider = lm.getBestProvider(criteria, true)

        createTimerTask()
        startForeground(NotificationId.RUN_TIMER, NotificationCompat.Builder(this, NotificationChannelId.TIMER).apply {
            setContentTitle(getString(R.string.running_noti_title))
            setContentText(String.format("%02d:%02d", second / 600, (second % 600) / 10))
            setSmallIcon(R.drawable.ic_run)
            color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

        }.build())
        timerStart = true

        LocalBroadcastManager.getInstance(this).registerReceiver(object: BroadcastReceiver() {
            override fun onReceive(context: Context, i: Intent) {
                if (i.getBooleanExtra(IntentValue.RUN_TIMER_STOP, false)) {
                    timerStart = false
                    stopForeground(true)
                }
            }
        }, IntentFilter(IntentKey.RUN_TIMER))

        /*
        if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationListener = object: LocationListener {
                override fun onLocationChanged(location: Location) {

                }
            }
            lm.requestLocationUpdates(locationProvider ?: LocationManager.GPS_PROVIDER, 100, 5f) { location ->
                val accuracy = location.accuracy
                val latitude = location.latitude
                val longitude = location.longitude

                if (lastLat == -1.0) lastLat = latitude
                if (lastLng == -1.0) lastLng = longitude

                if (lastLat != latitude && lastLng != longitude) {
                    val start = Location("start").apply {
                        setLatitude(lastLat)
                        setLongitude(lastLng)
                    }
                    val end = Location("end").apply {
                        setLatitude(latitude)
                        setLongitude(longitude)
                    }

                    if (timerStart) distance += start.distanceTo(end)

                }
                Log.d(TAG, distance.toString() + " ${lastLat} ${lastLng}")
                lastLat = latitude
                lastLng = longitude
            }

        }

         */
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    private fun getSecond(): Int = second

    @OptIn(DelicateCoroutinesApi::class)
    private fun createTimerTask() {
        val timerTask = object: TimerTask() {
            override fun run() {
                if (timerStart) {
                    second++
                    if (second % 10 == 0) {
                        nm.notify(NotificationId.RUN_TIMER, NotificationCompat.Builder(applicationContext, NotificationChannelId.TIMER).apply {
                            setContentTitle(getString(R.string.running_noti_title))
                            setContentText(String.format("%02d:%02d", second / 600, (second % 600) / 10))
                            setSmallIcon(R.drawable.ic_run)
                            setOngoing(true)
                            color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                        }.build())

                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(IntentKey.RUN_TIMER).apply {
                            putExtra(IntentValue.RUN_TIMER_SECOND, second)
                        })
                    }

                    if (second % 100 == 0) {

                    }
                }
            }
        }

        timer = Timer()
        timer.schedule(timerTask, 0, 100)
    }
}