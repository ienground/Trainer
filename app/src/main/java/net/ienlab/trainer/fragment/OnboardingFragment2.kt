package net.ienlab.trainer.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.ImageDecoder
import android.graphics.Typeface
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.canhub.cropper.*
import com.google.android.material.snackbar.Snackbar
import net.ienlab.trainer.databinding.FragmentOnboarding2Binding
import net.ienlab.trainer.R
import net.ienlab.trainer.activity.TAG
import net.ienlab.trainer.constant.SharedKey
import net.ienlab.trainer.utils.MyUtils.Companion.saveToInternalStorage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class OnboardingFragment2 : Fragment() {

    lateinit var binding: FragmentOnboarding2Binding
    private var mListener: OnFragmentInteractionListener? = null

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_onboarding2, container, false)
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
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -19)
        calendar.set(Calendar.MONTH, 0)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        binding.btnAction.text = dateFormat.format(calendar.time)

        binding.btnAction.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)

                sharedPreferences.edit().putInt(SharedKey.BIRTHDAY, dateSaveFormat.format(calendar.time).toInt()).apply()
                binding.btnAction.text = dateFormat.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.ivProfile.setOnClickListener {
            startCrop()
        }

        binding.etName.editText?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                sharedPreferences.edit().putString(SharedKey.NICKNAME, p0?.toString()).apply()
            }
        })
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent = result.uriContent
            val uriFilePath = result.getUriFilePath(requireContext()) // optional usage

            val wrapper = ContextWrapper(requireContext())
            val profileImageFile = File(wrapper.getDir("images", Context.MODE_PRIVATE), "profile.jpg")
            val inputStream = requireActivity().contentResolver.openInputStream(result.uriContent ?: Uri.EMPTY)
            val outputStream = FileOutputStream(profileImageFile)
            val buf = ByteArray(1024)
            var len: Int

            if (inputStream != null) {
                while (inputStream.read(buf).also { len = it } > 0) {
                    outputStream.write(buf, 0, len)
                }
                outputStream.close()
                inputStream.close()
                binding.ivProfile.setImageURI(profileImageFile.toUri())
                sharedPreferences.edit().putString(SharedKey.PROFILE_URI, profileImageFile.toUri().toString())
            }
        } else {
            // an error occurred
            val exception = result.error
        }
    }

    private fun startCrop() { // start picker to get image for cropping and then use the image in cropping activity
        cropImage.launch(options {
            setGuidelines(CropImageView.Guidelines.ON)
            setAspectRatio(1, 1)
            setFixAspectRatio(true)
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
        fun newInstance() = OnboardingFragment2().apply {
            val args = Bundle()
            arguments = args
        }
    }
}