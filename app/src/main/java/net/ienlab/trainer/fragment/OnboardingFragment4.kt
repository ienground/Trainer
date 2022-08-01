package net.ienlab.trainer.fragment

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import net.ienlab.trainer.databinding.FragmentOnboarding4Binding
import net.ienlab.trainer.R
import net.ienlab.trainer.constant.SharedKey

class OnboardingFragment4 : Fragment() {

    lateinit var binding: FragmentOnboarding4Binding
    private var mListener: OnFragmentInteractionListener? = null

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding4, container, false)
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

        binding.lvSlider.setLabelFormatter {
            if (it == 3f) getString(R.string.special_level) else getString(R.string.nlevel, 3 - it.toInt())
        }

        binding.lvSlider.addOnChangeListener { slider, value, fromUser ->
            sharedPreferences.edit().putInt(SharedKey.GOAL_LV, 3 - value.toInt()).apply()
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
        fun newInstance() = OnboardingFragment4().apply {
            val args = Bundle()
            arguments = args
        }
    }
}