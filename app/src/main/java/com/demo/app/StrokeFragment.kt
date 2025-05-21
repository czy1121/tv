package com.demo.app

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.fragment.app.Fragment
import com.demo.app.databinding.FragmentOtherBinding
import com.demo.app.databinding.FragmentStrokeBinding

class StrokeFragment: Fragment(R.layout.fragment_stroke) {
    private val binding by lazy { FragmentStrokeBinding.bind(requireView()) }
    class TextColorSpan(private val color: Int) : MetricAffectingSpan() {
        override fun updateMeasureState(textPaint: TextPaint) {
            if (textPaint.style != Paint.Style.STROKE) {
                textPaint.color = color
            }
        }

        override fun updateDrawState(textPaint: TextPaint) {
            if (textPaint.style != Paint.Style.STROKE) {
                textPaint.color = color
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.span.text = buildSpannedString {
            append("01234")
            inSpans(TextColorSpan(Color.LTGRAY)) {
                append("56789")
            }
            inSpans(RelativeSizeSpan(2f)) {
                append("0123456789")
            }
        }

//        binding.justTest.text = buildSpannedString {
//            inSpans(BackgroundColorSpan(Color.LTGRAY)) {
//                append("12.345")
//            }
//            inSpans(BackgroundColorSpan(Color.RED)) {
//                append("6789")
//            }
//
//        }
    }
}