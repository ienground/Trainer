package net.ienlab.trainer.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import net.ienlab.trainer.R
import net.ienlab.trainer.databinding.ActivityPushupAddBinding
import net.ienlab.trainer.utils.MyUtils
import java.util.*

class PushupAddActivity : AppCompatActivity() {

    private var second = 0
    private var timerStart = false
    private var timer = Timer()

    lateinit var binding: ActivityPushupAddBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pushup_add)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        second = 2 * 60 * 10
        var counter = 0

        binding.btnTimer.setOnClickListener {
            if (timerStart) {
                Log.d(TAG, "timer stop")
                timerStart = false
                timer.cancel()
            } else {
                Log.d(TAG, "timer start")
                timerStart = true
                createTimerTask()
            }

        }

        binding.cardCounter.setOnClickListener {
            if (timerStart) {
                binding.tvCounter.text = "${++counter}"
            }
        }

//        binding.progressTimer.setProgressFormatter { _, _ ->  "" }
//        binding.progressTimer.progress = 120
//        binding.progressTimer.progress = 110
    }

    private fun createTimerTask() {
        val timerTask = object: TimerTask() {
            override fun run() {
                if (timerStart) {
                    if (second > 0) {
                        second--
                        runOnUiThread {
                            binding.tvTimer.text = String.format("%02d:%02d", second / 600, second % 600)
                        }

                    } else {
                        timer.cancel()
                        timerStart = false
                    }
                }
            }
        }

        timer = Timer()
        timer.schedule(timerTask, 0, 100)
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