package com.demo.app

import android.os.Bundle
import android.text.style.RelativeSizeSpan
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.Fragment
import com.demo.app.databinding.FragmentOtherBinding
import com.demo.app.databinding.FragmentStrokeBinding

class StrokeFragment: Fragment(R.layout.fragment_stroke) {
    private val binding by lazy { FragmentStrokeBinding.bind(requireView()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.span.text = buildSpannedString {
            append("0123456789")
            inSpans(RelativeSizeSpan(2f)) {
                append("0123456789")
            }
        }
    }
}