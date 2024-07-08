package com.example.tudoem1

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.recyclerview.widget.RecyclerView
import com.example.tudoem1.NetMonsterActivity.Companion.REFRESH_RATIO
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.core.feature.detect.DetectorHspaDc
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedCellInfo
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedNrDisplayInfo
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedNrServiceState
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedPhysicalChannel

/**
 * Activity periodically updates data (once in [REFRESH_RATIO] ms) when it's on foreground.
 */
class NetMonsterActivity : AppCompatActivity() {

    companion object {
        private const val REFRESH_RATIO = 1_000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private val adapter = MainAdapter()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_netmonster)

        recyclerView = findViewById(R.id.recycler)
        recyclerView.adapter = adapter

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            loop()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            ), 0)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    private fun loop() {
        updateData()
        handler.postDelayed(REFRESH_RATIO) { loop() }
    }

    @SuppressLint("MissingPermission")
    private fun updateData() {
        NetMonsterFactory.get(this).apply {
            val merged = getCells()
            adapter.data = merged
//            val subset = getCells( // subset of available sources
//                CellSource.ALL_CELL_INFO,
//                CellSource.CELL_LOCATION
//            )
//            adapter.data = subset
            Log.d("NTM-RES", " \n${merged.joinToString(separator = "\n")}")
//            Log.d("NTM-Subset", " \n${subset.joinToString(separator = "\n")}")
            // All detectors that are bundled in NetMonster Core
            val networkType : NetworkType = getNetworkType(0)
            Log.d("NetworkType", " $networkType")
            // Only HSPA+42 (guess, not from RIL)
            val isHspaDc: NetworkType? = getNetworkType(0, DetectorHspaDc())
            Log.d("NetworkType", " $isHspaDc")
            // LTE-A from CellInfo (guess, not from RIL), NSA NR
            val isLteCaCellInfo: NetworkType? = getNetworkType(0, DetectorLteAdvancedCellInfo())
            Log.d("NetworkType", " $isLteCaCellInfo")
            // LTE-A from ServiceState (from RIL, Android P+)
            val isLteCaServiceState: NetworkType? = getNetworkType(0, DetectorLteAdvancedNrServiceState())
            Log.d("NetworkType", " $isLteCaServiceState")

            // LTE-A from PhysicalChannel (from RIL, Android P+)
            val isLteCaPhysicalChannel: NetworkType? = getNetworkType(0, DetectorLteAdvancedPhysicalChannel())
            Log.d("NetworkType", " $isLteCaPhysicalChannel")

            // LTE-A and NR from DisplayInfo (marketing purposes, might result false-positive data, Android R+)
            // You can also detect only LTE-A or NR using one of classes:
            // - DetectorLteAdvancedServiceState ... for LTE-A
            // - DetectorNsaNr ... for NR NSA
            val isLteCaOrNsaNrDisplayInfo: NetworkType? = getNetworkType(0, DetectorLteAdvancedNrDisplayInfo())
            Log.d("NetworkType", " $isLteCaOrNsaNrDisplayInfo")

        }
    }

}
