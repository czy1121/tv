package com.demo.app

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.demo.app.databinding.FragmentTestBinding

class TestFragment : Fragment(R.layout.fragment_test) {

    private val binding by lazy { FragmentTestBinding.bind(requireView()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}