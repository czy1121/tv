package com.demo.app

import androidx.fragment.app.Fragment
import com.demo.app.databinding.FragmentOtherBinding
import com.demo.app.databinding.FragmentStrokeBinding

class StrokeFragment: Fragment(R.layout.fragment_stroke) {
    private val binding by lazy { FragmentStrokeBinding.bind(requireView()) }

}