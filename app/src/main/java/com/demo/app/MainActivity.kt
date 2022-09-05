package com.demo.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import me.reezy.cosmo.tv.ThreeTextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var gravity = 0
        var size = 12
        var text = ""
        findViewById<View>(R.id.three).setOnClickListener {
            text += "12345"
            (it as ThreeTextView).text = text
        }
    }
}