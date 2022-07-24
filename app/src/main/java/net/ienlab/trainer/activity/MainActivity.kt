package net.ienlab.trainer.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import net.ienlab.trainer.R
import net.ienlab.trainer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        binding.btnAdd.setOnClickListener {
            binding.groupAdd.animate()
                .alpha(1f)
                .setDuration(300)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        binding.groupAdd.visibility = View.VISIBLE
                    }
                })
        }

        val closeClickListener = View.OnClickListener {
            binding.groupAdd.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.groupAdd.visibility = View.GONE
                    }
                })
        }

        binding.btnClose.setOnClickListener(closeClickListener)
//        binding.blurAdd.setOnClickListener(closeClickListener)

        binding.addCardRunning.setOnClickListener {
            startActivity(Intent(this, RunAddActivity::class.java))
            binding.groupAdd.visibility = View.GONE
        }
    }
}