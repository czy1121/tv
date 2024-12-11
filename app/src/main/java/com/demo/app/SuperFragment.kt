package com.demo.app

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.demo.app.databinding.FragmentSuperBinding
import me.reezy.cosmo.tv.SuperTextView

class SuperFragment: Fragment(R.layout.fragment_super) {


    private val binding by lazy { FragmentSuperBinding.bind(requireView()) }

    private val tvGravity = listOf(
        SuperTextView.GRAVITY_START,
        SuperTextView.GRAVITY_END,
        SuperTextView.GRAVITY_TOP,
        SuperTextView.GRAVITY_BOTTOM,

        SuperTextView.GRAVITY_TEXT_START,
        SuperTextView.GRAVITY_TEXT_END,
        SuperTextView.GRAVITY_TEXT_TOP,
        SuperTextView.GRAVITY_TEXT_BOTTOM,
    )

    private val tvTextGravity = listOf(
        Gravity.START or Gravity.TOP,
        Gravity.START or Gravity.CENTER_VERTICAL,
        Gravity.START or Gravity.BOTTOM,

        Gravity.CENTER_HORIZONTAL or Gravity.TOP,
        Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL,
        Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM,

        Gravity.END or Gravity.TOP,
        Gravity.END or Gravity.CENTER_VERTICAL,
        Gravity.END or Gravity.BOTTOM,
    )


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.spIconGravity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                binding.superText.iconGravity = tvGravity[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        binding.spIconGravity.setSelection(2)

        binding.spTextGravity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                binding.superText.gravity = tvTextGravity[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        binding.spTextGravity.setSelection(0)

        binding.spSubtextGravity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                binding.superText.subtextGravity = tvGravity[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        binding.spSubtextGravity.setSelection(2)
    }
}