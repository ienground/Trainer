package net.ienlab.trainer.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.ikovac.timepickerwithseconds.MyTimePickerDialog
import com.ikovac.timepickerwithseconds.TimePicker
import net.ienlab.trainer.databinding.FragmentOnboarding5Binding
import net.ienlab.trainer.R
import net.ienlab.trainer.constant.SharedKey
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class OnboardingFragment5 : Fragment() {

    lateinit var binding: FragmentOnboarding5Binding
    private var mListener: OnFragmentInteractionListener? = null

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding5, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("${requireContext().packageName}_preferences", Context.MODE_PRIVATE)

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val runTime = Calendar.getInstance()
        runTime.set(Calendar.HOUR_OF_DAY, 0)
        runTime.set(Calendar.MINUTE, 0)
        runTime.set(Calendar.SECOND, 0)
        binding.btnRun.text = timeFormat.format(runTime.time)
        sharedPreferences.edit().putInt(SharedKey.TEMP_RUN, -1).apply()

        binding.btnRun.setOnClickListener {
            MyTimePickerDialog(requireContext(), { view, hourOfDay, minute, seconds ->
                runTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                runTime.set(Calendar.MINUTE, minute)
                runTime.set(Calendar.SECOND, seconds)
                binding.btnRun.text = timeFormat.format(runTime.time)
                sharedPreferences.edit().putInt(SharedKey.TEMP_RUN, (hourOfDay * 3600 + minute * 60 + seconds) * 10).apply()
            }, runTime.get(Calendar.HOUR_OF_DAY), runTime.get(Calendar.HOUR_OF_DAY), runTime.get(Calendar.HOUR_OF_DAY), true).show()
        }

        binding.etPushup.editText?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                sharedPreferences.edit().putInt(SharedKey.TEMP_PUSHUP,
                    if (p0?.isNotEmpty() == true) p0.toString().toInt() else -1).apply()
            }
        })

        binding.etSitup.editText?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                sharedPreferences.edit().putInt(SharedKey.TEMP_SITUP,
                    if (p0?.isNotEmpty() == true) p0.toString().toInt() else -1).apply()
            }
        })

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() = OnboardingFragment5().apply {
            val args = Bundle()
            arguments = args
        }
    }
}