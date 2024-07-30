package com.example.tudoem1

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.tudoem1.services.NetMonsterService

class NetMonsterFragment : Fragment() {

    private val adapter = MainAdapter()
    private lateinit var recyclerView: RecyclerView
    private lateinit var stopMeasureButton: Button
    private lateinit var showLat: TextView
    private lateinit var showLong: TextView

    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NetMonsterService.ACTION_DATA_UPDATED) {
                val metricsJson = intent.getStringExtra(NetMonsterService.EXTRA_METRICS)
                Log.d("NetMonsterFragment", "Received metrics: $metricsJson")
                adapter.data = NetMonsterService.merged

                showLat.text = "Lat: ${intent.getStringExtra("Lat")}"
                showLong.text = "Long: ${intent.getStringExtra("Long")}"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_main_netmonster, container, false)
        showLat = view.findViewById(R.id.toolbar_title)
        showLong = view.findViewById(R.id.toolbar_title1)
        recyclerView = view.findViewById(R.id.recycler)
        recyclerView.adapter = adapter

        stopMeasureButton = view.findViewById(R.id.my_button)

        stopMeasureButton.setOnClickListener {
            context?.unregisterReceiver(dataReceiver)
            val intent = Intent(context, NetMonsterService::class.java)
            intent.action = NetMonsterService.ACTION_STOP_SERVICE
            context?.startService(intent)
        }
        return view
    }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.registerReceiver(
            dataReceiver, IntentFilter(NetMonsterService.ACTION_DATA_UPDATED),
            AppCompatActivity.RECEIVER_EXPORTED
        )
        startNetMonsterService()

    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(dataReceiver)
    }

    private fun startNetMonsterService() {
        val intent = Intent(context, NetMonsterService::class.java)
        intent.action = NetMonsterService.ACTION_START_SERVICE
        context?.startService(intent)
    }
}
