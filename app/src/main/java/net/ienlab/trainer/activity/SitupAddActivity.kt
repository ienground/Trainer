package net.ienlab.trainer.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import net.ienlab.trainer.R
import net.ienlab.trainer.databinding.ActivitySitupAddBinding
import net.ienlab.trainer.room.TrainingDatabase
import net.ienlab.trainer.room.TrainingEntity
import net.ienlab.trainer.utils.MyBottomSheetDialog
import java.util.*

class SitupAddActivity : AppCompatActivity() {

    private var second = 0
    private var counter = 0
    private var timerStart = false
    private var timer = Timer()
    private var trainingDatabase: TrainingDatabase? = null

    lateinit var binding: ActivitySitupAddBinding

    @OptIn(DelicateCoroutinesApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_situp_add)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        trainingDatabase = TrainingDatabase.getInstance(this)

//        val gifImage = Glide
        Glide.with(this)
            .asBitmap()
            .load(R.drawable.ic_situp_gif).into(binding.icSitup)

        second = 2 * 60 * 10
        counter = 0

        binding.progressTimer.max = 1200f
        binding.progressTimer.progress = 1200f

        binding.btnTimer.setOnClickListener {
            if (timerStart) {
                Log.d(TAG, "timer stop")
                timerStart = false
                binding.btnTimer.setIconResource(R.drawable.ic_play)
                binding.btnTimer.text = getString(R.string.start)
                Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.ic_situp_gif).into(binding.icSitup)
                timer.cancel()
            } else {
                Log.d(TAG, "timer start")
                timerStart = true
                binding.btnTimer.setIconResource(R.drawable.ic_pause)
                binding.btnTimer.text = getString(R.string.pause)
                Glide.with(this)
                    .asGif()
                    .load(R.drawable.ic_situp_gif).into(binding.icSitup)
                createTimerTask()
            }

        }

        binding.cardCounter.setOnClickListener {
            if (timerStart) {
                binding.tvCounter.text = "${++counter}"
            }
        }

        binding.tvFillManual.paint.isUnderlineText = true
        binding.tvFillManual.setOnClickListener {
            MyBottomSheetDialog(this@SitupAddActivity).apply {
                val dialogView = layoutInflater.inflate(R.layout.dialog_edittext, LinearLayout(applicationContext), false)
                val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
                val tvContent: TextInputLayout = dialogView.findViewById(R.id.tv_content)
                val btnPositive: LinearLayout = dialogView.findViewById(R.id.btn_positive)
                val btnNegative: LinearLayout = dialogView.findViewById(R.id.btn_negative)
                val tvPositive: TextView = dialogView.findViewById(R.id.btn_positive_text)
                val tvNegative: TextView = dialogView.findViewById(R.id.btn_negative_text)

                tvTitle.text = getString(R.string.comfirm_record)
                tvContent.editText?.hint = getString(R.string.record)
                tvContent.editText?.setText(counter.toString())

                btnPositive.setOnClickListener {
                    GlobalScope.launch(Dispatchers.IO) {
                        val entity = tvContent.editText?.text?.toString()?.let {
                            TrainingEntity(System.currentTimeMillis(), TrainingEntity.TYPE_SITUP, if (it.isNotEmpty()) it.toInt() else 0)
                        }
                        if (entity != null) {
                            trainingDatabase?.getDao()?.add(entity)
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SitupAddActivity, getString(R.string.saved), Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        }
                    }
                }

                btnNegative.setOnClickListener {
                    dismiss()
                    finish()
                }

                setContentView(dialogView)
            }.show()

        }

        binding.tvFillManual.setOnClickListener {
            MyBottomSheetDialog(this@SitupAddActivity).apply {
                val dialogView = layoutInflater.inflate(R.layout.dialog_edittext, LinearLayout(applicationContext), false)
                val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
                val tvContent: TextInputLayout = dialogView.findViewById(R.id.tv_content)
                val btnPositive: LinearLayout = dialogView.findViewById(R.id.btn_positive)
                val btnNegative: LinearLayout = dialogView.findViewById(R.id.btn_negative)
                val tvPositive: TextView = dialogView.findViewById(R.id.btn_positive_text)
                val tvNegative: TextView = dialogView.findViewById(R.id.btn_negative_text)

                tvTitle.text = getString(R.string.comfirm_record)
                tvContent.editText?.hint = getString(R.string.record)
                tvContent.editText?.setText(counter.toString())

                btnPositive.setOnClickListener {
                    GlobalScope.launch(Dispatchers.IO) {
                        val entity = tvContent.editText?.text?.toString()?.let {
                            TrainingEntity(System.currentTimeMillis(), TrainingEntity.TYPE_SITUP, if (it.isNotEmpty()) it.toInt() else 0)
                        }
                        if (entity != null) {
                            trainingDatabase?.getDao()?.add(entity)
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SitupAddActivity, getString(R.string.saved), Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        }
                    }
                }

                btnNegative.setOnClickListener {
                    dismiss()
                    finish()
                }

                setContentView(dialogView)
            }.show()
        }

//        binding.progressTimer.setProgressFormatter { _, _ ->  "" }
//        binding.progressTimer.progress = 120
//        binding.progressTimer.progress = 110
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun createTimerTask() {
        val timerTask = object: TimerTask() {
            override fun run() {
                if (timerStart) {
                    if (second > 0) {
                        second--
                        runOnUiThread {
                            binding.tvTimer.text = String.format("%02d:%02d", second / 600, (second % 600) / 10)
                            binding.progressTimer.progress = second.toFloat()
                        }

                    } else {
                        timer.cancel()
                        timerStart = false

                        runOnUiThread {
                            Glide.with(this@SitupAddActivity)
                                .asBitmap()
                                .load(R.drawable.ic_situp_gif).into(binding.icSitup)

                            binding.progressTimer.progress = second.toFloat()
                            MyBottomSheetDialog(this@SitupAddActivity).apply {
                                val dialogView = layoutInflater.inflate(R.layout.dialog_edittext, LinearLayout(applicationContext), false)
                                val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
                                val tvContent: TextInputLayout = dialogView.findViewById(R.id.tv_content)
                                val btnPositive: LinearLayout = dialogView.findViewById(R.id.btn_positive)
                                val btnNegative: LinearLayout = dialogView.findViewById(R.id.btn_negative)
                                val tvPositive: TextView = dialogView.findViewById(R.id.btn_positive_text)
                                val tvNegative: TextView = dialogView.findViewById(R.id.btn_negative_text)

                                //                            tvTitle.typeface = typefaceBold
                                //                            tvContent.typeface = typefaceRegular
                                //                            tvPositive.typeface = typefaceRegular
                                //                            tvNegative.typeface = typefaceRegular

                                tvTitle.text = getString(R.string.comfirm_record)
                                tvContent.editText?.hint = getString(R.string.record)
                                tvContent.editText?.setText(counter.toString())
                                //                            tvContent.text = getString(R.string.delete_confirm)

                                btnPositive.setOnClickListener {
                                    GlobalScope.launch(Dispatchers.IO) {
                                        val entity = tvContent.editText?.text?.toString()?.let {
                                            TrainingEntity(System.currentTimeMillis(), TrainingEntity.TYPE_SITUP, if (it.isNotEmpty()) it.toInt() else 0)
                                        }
                                        if (entity != null) {
                                            trainingDatabase?.getDao()?.add(entity)
                                        }
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(this@SitupAddActivity, getString(R.string.saved), Toast.LENGTH_SHORT).show()
                                            setResult(RESULT_OK)
                                            finish()
                                        }
                                    }
                                }

                                btnNegative.setOnClickListener {
                                    dismiss()
                                    finish()
                                }

                                setContentView(dialogView)
                            }.show()
                        }

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