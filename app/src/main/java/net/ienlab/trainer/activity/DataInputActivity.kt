package net.ienlab.trainer.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.databinding.DataBindingUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ienlab.trainer.R
import net.ienlab.trainer.databinding.ActivityDataInputBinding
import net.ienlab.trainer.room.TrainingDatabase
import net.ienlab.trainer.room.TrainingEntity
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

    }
}