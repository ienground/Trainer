package net.ienlab.trainer.preference

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import net.ienlab.trainer.R
import net.ienlab.trainer.constant.SharedKey


class AppInfoPreference(context: Context, attrs: AttributeSet): Preference(context, attrs) {
    init {
        widgetLayoutResource = R.layout.preference_app_info
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
    }
}