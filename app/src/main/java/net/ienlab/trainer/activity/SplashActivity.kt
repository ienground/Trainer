package net.ienlab.trainer.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import kotlinx.coroutines.*
import net.ienlab.trainer.constant.SharedKey
import net.ienlab.trainer.R
import net.ienlab.trainer.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding

    // 로딩 화면이 떠있는 시간(밀리초단위)
    private val SPLASH_DISPLAY_LENGTH = 1000

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.activity = this

        val sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        val isFirstVisit = sharedPreferences.getBoolean(SharedKey.IS_FIRST_VISIT, true)

        Handler(Looper.getMainLooper()).postDelayed({
            val mainIntent = Intent(this, MainActivity::class.java).apply {
//                putExtra(IntentKey.PLAN_ID, intent.getIntExtra(IntentKey.PLAN_ID, -1))
            }
            val welcomeIntent = Intent(this, OnboardingActivity::class.java)
            if (isFirstVisit) {
                startActivity(welcomeIntent)
                finish()
            } else {
                val handler = MainActivityOpenHandler(this, mainIntent)
                Thread {
                    handler.sendEmptyMessage(0)
                }.start()
                finish()
            }
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}

class MainActivityOpenHandler(val activity: Activity, val intent: Intent): Handler(Looper.getMainLooper()) {
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        activity.startActivity(intent)
    }
}
