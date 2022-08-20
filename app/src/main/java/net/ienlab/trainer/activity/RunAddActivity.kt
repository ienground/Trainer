package net.ienlab.trainer.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.ikovac.timepickerwithseconds.MyTimePickerDialog
import kotlinx.coroutines.*
import net.ienlab.trainer.R
import net.ienlab.trainer.constant.IntentKey
import net.ienlab.trainer.constant.IntentValue
import net.ienlab.trainer.constant.SharedKey
import net.ienlab.trainer.databinding.ActivityRunAddBinding
import net.ienlab.trainer.room.TrainingDatabase
import net.ienlab.trainer.room.TrainingEntity
import net.ienlab.trainer.service.RunTimerService
import net.ienlab.trainer.utils.MyBottomSheetDialog
import java.util.*

class RunAddActivity : AppCompatActivity() {

    private var second = 0
    private var timerStart = false
    private var timer = Timer()
    private var trainingDatabase: TrainingDatabase? = null

    lateinit var binding: ActivityRunAddBinding

    @OptIn(DelicateCoroutinesApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_run_add)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        trainingDatabase = TrainingDatabase.getInstance(this)

        binding.tvFillManual.paint.isUnderlineText = true

        LocalBroadcastManager.getInstance(this).registerReceiver(object: BroadcastReceiver() {
            override fun onReceive(context: Context, i: Intent) {
                second = i.getIntExtra(IntentValue.RUN_TIMER_SECOND, 0)
                binding.tvTimer.text = String.format("%02d:%02d", second / 600, (second % 600) / 10)
            }
        }, IntentFilter(IntentKey.RUN_TIMER))

        binding.btnTimer.setOnClickListener {
            if (timerStart) {
                Log.d(TAG, "timer stop")
                timerStart = false
                binding.btnTimer.setIconResource(R.drawable.ic_play)
                binding.btnTimer.text = getString(R.string.start)
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(IntentKey.RUN_TIMER).apply {
                    putExtra(IntentValue.RUN_TIMER_STOP, true)
                })

                runOnUiThread {
                    MyTimePickerDialog(this@RunAddActivity, { view, hourOfDay, minute, seconds ->
                        GlobalScope.launch(Dispatchers.IO) {
                            val entity = TrainingEntity(System.currentTimeMillis(), TrainingEntity.TYPE_RUN, minute * 600 + seconds * 10)
                            trainingDatabase?.getDao()?.add(entity)
                            Log.d(TAG, entity.toString())
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@RunAddActivity, getString(R.string.saved), Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK)
                                finish()
                            }
                        }
                    }, 0, second / 600, (second % 600) / 10, true).apply {
                        setOnCancelListener {
                            finish()
                        }
                    }
                        .show()
                }
            } else {
                Log.d(TAG, "timer start")
                timerStart = true
                binding.btnTimer.setIconResource(R.drawable.ic_stop)
                binding.btnTimer.text = getString(R.string.stop)

                startForegroundService(Intent(this, RunTimerService::class.java))
//                createTimerTask()
            }

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

    override fun onBackPressed() {
        super.onBackPressed()
    }
}