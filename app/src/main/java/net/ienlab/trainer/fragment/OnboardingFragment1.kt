package net.ienlab.trainer.fragment

import android.Manifest
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import net.ienlab.trainer.databinding.FragmentOnboarding1Binding
import net.ienlab.trainer.R

class OnboardingFragment1 : Fragment() {

    lateinit var binding: FragmentOnboarding1Binding
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding1, container, false)
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

        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted -> // Handle Permission granted/rejected
            if (isGranted) { // Permission is granted
                binding.btnAction.text = getString(R.string.intro_page1_btn_confirmed)
            } else { // Permission is denied
                Snackbar.make(view, getString(R.string.permission_granted_msg), Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btnAction.setOnClickListener {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
        fun newInstance() = OnboardingFragment1().apply {
            val args = Bundle()
            arguments = args
        }
    }
}