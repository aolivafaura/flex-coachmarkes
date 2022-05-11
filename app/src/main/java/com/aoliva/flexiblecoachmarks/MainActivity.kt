package com.aoliva.flexiblecoachmarks

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.tvRegular).setOnClickListener {
            val intent = Intent(this@MainActivity, RegularUsageActivity::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.tvWindow).setOnClickListener {
            val intent = Intent(this@MainActivity, PassingWindowActivity::class.java)
            startActivity(intent)
        }
    }
}
