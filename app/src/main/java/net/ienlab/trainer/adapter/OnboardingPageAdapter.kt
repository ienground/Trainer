package net.ienlab.trainer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.ienlab.trainer.R
import net.ienlab.trainer.fragment.*

class OnboardingPageAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragment0()
            1 -> OnboardingFragment1()
            2 -> OnboardingFragment2()
            3 -> OnboardingFragment3()
            4 -> OnboardingFragment4()
            5 -> OnboardingFragment5()
            else -> OnboardingFragment0()
        }
    }

    override fun getItemCount(): Int = PAGE_NUMBER

    companion object {
        internal const val PAGE_NUMBER = 6
    }
}