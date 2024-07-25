package com.example.tudoem1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator


class AllMeasuresActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_measures)


        val viewPager = findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.view_pager)
        val tabs = findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabs)

        viewPager.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Sensors"
                1 -> "Network QoS"
                else -> null
            }
        }.attach()
    }
}