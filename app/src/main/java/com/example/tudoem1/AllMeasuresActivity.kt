package com.example.tudoem1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.tudoem1.view.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout


class AllMeasuresActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_measures)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        viewPager.setAdapter(ViewPagerAdapter(supportFragmentManager))

        tabLayout.setupWithViewPager(viewPager)
    }
}