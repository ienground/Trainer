package net.ienlab.trainer.fragment

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import net.ienlab.trainer.databinding.FragmentOnboarding5Binding
import net.ienlab.trainer.R

class OnboardingFragment5 : Fragment() {

    lateinit var binding: FragmentOnboarding5Binding
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding5, container, false)
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val typefaceRegular = ResourcesCompat.getFont(requireContext(), R.font.pretendard_regular) ?: Typeface.DEFAULT
        val typefaceBold = ResourcesCompat.getFont(requireContext(), R.font.pretendard_black) ?: Typeface.DEFAULT

        binding.tvPage.typeface = typefaceBold
        binding.tvTitle.typeface = typefaceBold
        binding.tvContent.typeface = typefaceRegular

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