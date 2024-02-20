package com.demo.app

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.demo.app.databinding.FragmentOtherBinding

class OtherFragment: Fragment(R.layout.fragment_other) {

    private val binding by lazy { FragmentOtherBinding.bind(requireView()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.marquee.isSelected = true
        binding.card.isVisible = true
    }
}