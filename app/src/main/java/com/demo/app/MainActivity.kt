package com.demo.app

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.demo.app.databinding.ActivityMainBinding
import me.reezy.cosmo.tabs.TabItem
import me.reezy.cosmo.tabs.setup
import me.reezy.cosmo.tv.LiteTextView
import me.reezy.cosmo.tv.MarqueeTextView

class MainActivity : AppCompatActivity(R.layout.activity_main) {


    private val binding by lazy { ActivityMainBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }

    private val items = arrayOf("TextView", "LiteTextView", "TwoTextView", )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.pager.offscreenPageLimit = 30
        binding.pager.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                return when(position) {
                    0 -> OtherFragment()
                    1 -> LiteFragment()
                    2 -> TwoFragment()
                    else -> Fragment()
                }
            }

            override fun getItemCount(): Int = items.size
        }

        binding.tabs.setup(items.map { TabItem(it, it) }, binding.pager) {
            textView?.textSize = 18f
            textView?.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        }
    }
}