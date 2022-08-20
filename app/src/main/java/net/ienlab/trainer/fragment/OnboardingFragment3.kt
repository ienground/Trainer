package net.ienlab.trainer.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import net.ienlab.trainer.databinding.FragmentOnboarding3Binding
import net.ienlab.trainer.R
import net.ienlab.trainer.constant.SharedKey
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*

class OnboardingFragment3 : Fragment() {

    lateinit var binding: FragmentOnboarding3Binding
    private var mListener: OnFragmentInteractionListener? = null
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding3, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val typefaceRegular = ResourcesCompat.getFont(requireContext(), R.font.pretendard_regular) ?: Typeface.DEFAULT
        val typefaceBold = ResourcesCompat.getFont(requireContext(), R.font.pretendard_black) ?: Typeface.DEFAULT

        sharedPreferences = requireContext().getSharedPreferences("${requireContext().packageName}_preferences", Context.MODE_PRIVATE)
        binding.tvPage.typeface = typefaceBold
        binding.tvTitle.typeface = typefaceBold
        binding.tvContent.typeface = typefaceRegular

        val dateFormat = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        val dateSaveFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val startCalendar = Calendar.getInstance()
        val endCalendar = Calendar.getInstance()
        endCalendar.add(Calendar.MONTH, 18)
        endCalendar.add(Calendar.DAY_OF_MONTH, -1)

        binding.btnAction.text = dateFormat.format(startCalendar.time)
        binding.btnAction2.text = dateFormat.format(endCalendar.time)

        binding.btnAction.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                startCalendar.set(Calendar.YEAR, year)
                startCalendar.set(Calendar.MONTH, month)
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)

                endCalendar.time = startCalendar.time
                endCalendar.add(Calendar.MONTH, 18)
                endCalendar.add(Calendar.DAY_OF_MONTH, -1)

                sharedPreferences.edit().putInt(SharedKey.INPUT_DAY, dateSaveFormat.format(startCalendar.time).toInt()).apply()
                sharedPreferences.edit().putInt(SharedKey.OUTPUT_DAY, dateSaveFormat.format(endCalendar.time).toInt()).apply()
                binding.btnAction.text = dateFormat.format(startCalendar.time)
                binding.btnAction2.text = dateFormat.format(endCalendar.time)

                val monthDiff = Calendar.getInstance().let { it.get(Calendar.YEAR) * 12 + it.get(Calendar.MONTH) } - startCalendar.let { it.get(Calendar.YEAR) * 12 + it.get(Calendar.MONTH) }
                if (monthDiff <= 2) {
                    sharedPreferences.edit().putInt(SharedKey.GRADE, 10 + monthDiff).apply()
                } else if (monthDiff <= 8) {
                    sharedPreferences.edit().putInt(SharedKey.GRADE, 20 + monthDiff - 2).apply()
                } else if (monthDiff <= 14) {
                    sharedPreferences.edit().putInt(SharedKey.GRADE, 30 + monthDiff - 8).apply()
                } else {
                    sharedPreferences.edit().putInt(SharedKey.GRADE, 40 + monthDiff - 14).apply()
                }
            }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnAction2.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                endCalendar.set(Calendar.YEAR, year)
                endCalendar.set(Calendar.MONTH, month)
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                endCalendar.set(Calendar.HOUR_OF_DAY, 0)
                endCalendar.set(Calendar.MINUTE, 0)
                endCalendar.set(Calendar.SECOND, 0)

                sharedPreferences.edit().putInt(SharedKey.OUTPUT_DAY, dateSaveFormat.format(endCalendar.time).toInt()).apply()
                binding.btnAction2.text = dateFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }


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
        fun newInstance() = OnboardingFragment3().apply {
            val args = Bundle()
            arguments = args
        }
    }
}