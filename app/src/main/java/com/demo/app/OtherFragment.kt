package com.demo.app

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.demo.app.databinding.FragmentOtherBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.reezy.cosmo.tv.countingtextview.CountingAnimator
import kotlin.random.Random

class OtherFragment : Fragment(R.layout.fragment_other) {

    private val binding by lazy { FragmentOtherBinding.bind(requireView()) }

    private val animator by lazy { CountingAnimator("0", 2 * 1000).attachTo(binding.counting, true) }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(1000)
            binding.justTest.text = Random.nextInt(111111, 999999).toString()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.marquee.isSelected = true
        binding.card.isVisible = true

        binding.justTest.text = buildSpannedString {
            append("456")
        }

        binding.justTest.setOnClickListener {
            binding.justTest.text = Random.nextInt(111111, 999999).toString()
        }

//        lifecycleScope.launch {
//            while (true) {
//                delay(5000)
//                binding.justTest.text = Random.nextInt(111111, 999999).toString()
//            }
//        }

        val max = 1000000000000.0
        var factor = 10.0
        binding.btnDecrease.setOnClickListener {
            factor = 0.1
            animator.count(max, max)
        }
        binding.btnIncrease.setOnClickListener {
            factor = 10.0
            animator.count(0.0, 1.0)
        }
        animator.doOnEnd {
            val now = animator.animatedValue as Double
            if (factor > 1) {
                if (now < max) {
                    animator.countTo(now * 10 + 1)
                }
            } else {
                if (now >= 1) {
                    animator.countTo(now * factor)
                }
            }
        }


    }
}