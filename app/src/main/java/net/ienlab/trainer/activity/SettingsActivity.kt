package net.ienlab.trainer.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Typeface
import android.net.Uri
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import net.ienlab.trainer.BuildConfig
import net.ienlab.trainer.R
import net.ienlab.trainer.constant.*
import net.ienlab.trainer.databinding.ActivitySettingsBinding
import net.ienlab.trainer.room.*
import net.ienlab.trainer.utils.MyBottomSheetDialog
import net.ienlab.trainer.utils.MyUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        binding.activity = this

        val typefaceBold = ResourcesCompat.getFont(this, R.font.pretendard_black) ?: Typeface.DEFAULT

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, SettingsPreferenceFragment())
                .commit()
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
        setResult(RESULT_OK)
        super.onBackPressed()
    }

    class SettingsPreferenceFragment: PreferenceFragmentCompat() {

        lateinit var am: AlarmManager
        lateinit var typefaceBold: Typeface
        lateinit var typefaceRegular: Typeface
        lateinit var sharedPreferences: SharedPreferences

        private val timeFormat = SimpleDateFormat("a h:mm", Locale.getDefault())

        @OptIn(DelicateCoroutinesApi::class)
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val appInfo = findPreference<Preference>("app_title")
            val changelog = findPreference<Preference>("changelog")
            val email = findPreference<Preference>("ask_to_dev")
            val openSource = findPreference<Preference>("open_source")

            typefaceRegular = ResourcesCompat.getFont(requireContext(), R.font.pretendard_regular) ?: Typeface.DEFAULT
            typefaceBold = ResourcesCompat.getFont(requireContext(), R.font.pretendard_black) ?: Typeface.DEFAULT
            sharedPreferences = requireContext().getSharedPreferences("${requireContext().packageName}_preferences", Context.MODE_PRIVATE)

            appInfo?.setOnPreferenceClickListener {
                MyBottomSheetDialog(requireContext()).apply {
                    val view = layoutInflater.inflate(R.layout.dialog_changelog, LinearLayout(requireContext()), false)
                    val tvTitle: TextView = view.findViewById(R.id.tv_title)
                    val tvContent: TextView = view.findViewById(R.id.tv_content)

                    tvTitle.text = getString(R.string.app_name)
                    tvContent.text = getString(R.string.dev_milifare)
                    setContentView(view)
                }.show()

                true
            }


            changelog?.setOnPreferenceClickListener {
                MyBottomSheetDialog(requireContext()).apply {
                    val view = layoutInflater.inflate(R.layout.dialog_changelog, LinearLayout(requireContext()), false)
                    val tvTitle: TextView = view.findViewById(R.id.tv_title)
                    val tvContent: TextView = view.findViewById(R.id.tv_content)

                    tvTitle.text = "${getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}"
                    tvContent.text = MyUtils.fromHtml(MyUtils.readTextFromRaw(resources, R.raw.changelog))

                    setContentView(view)
                }.show()

                true
            }

            email?.setOnPreferenceClickListener {
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("admin@ienlab.net"))
                    putExtra(Intent.EXTRA_SUBJECT, "${getString(R.string.app_name)} ${BuildConfig.VERSION_NAME} ${getString(R.string.ask)}")
                    putExtra(Intent.EXTRA_TEXT, "${getString(R.string.email_text)}\n${Build.BRAND} ${Build.MODEL} Android ${Build.VERSION.RELEASE}\n_\n")
                    type = "message/rfc822"
                    startActivity(this)
                }
                true
            }

            openSource?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
                true
            }
        }
    }
}