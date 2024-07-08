package com.example.tudoem1

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
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
import com.example.tudoem1.gpsUtils.Types
import com.example.tudoem1.webservices.Coordinates
import com.example.tudoem1.webservices.Measure
import com.example.tudoem1.webservices.PostData
import com.example.tudoem1.webservices.retrofitInterface
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.core.feature.detect.DetectorHspaDc
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedCellInfo
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedNrDisplayInfo
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedNrServiceState
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedPhysicalChannel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    private var locationCoordinates = Types.DbCoordinates(0.0,0.0)
    private var broadcastReceiver: BroadcastReceiver? = null

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
            val intent = Intent(
                this@NetMonsterActivity,
                MainActivity::class.java
            )
            startActivity(intent)
            finish()
        }
    }

    private val gpsSwitchStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action) {
                val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (!isGpsEnabled ){
                    enableGPS()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED)

        registerReceiver(gpsSwitchStateReceiver, filter, )


        if (broadcastReceiver == null) {
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    println(intent.extras?.getDouble("Latitude"))
                    println(intent.extras?.getDouble("Longitude"))
                    locationCoordinates.lat = intent.extras?.getDouble("Latitude")!!
                    locationCoordinates.long = intent.extras?.getDouble("Longitude")!!
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("location_update"))
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            registerReceiver(broadcastReceiver, IntentFilter("location_update"), RECEIVER_EXPORTED)
//        }
//
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loop()
        } else requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            ), 0
        )

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
        Log.d("Coordinates", "$locationCoordinates")
//        val dateTime = getCurrentDateTime()
        NetMonsterFactory.get(this).apply {
            val merged = getCells()
            adapter.data = merged
//            val subset = getCells( // subset of available sources
//                CellSource.ALL_CELL_INFO,
//                CellSource.CELL_LOCATION
//            )
//            adapter.data = subset
            Log.d("NTM-RES", " \n${merged.joinToString(separator = "\n")}")


            val metrics = merged.joinToString(separator = "\n")
            val coordinates = Coordinates(locationCoordinates.lat, locationCoordinates.long)
            val measure = Measure(getCurrentDateTime(), coordinates, metrics)
            val postData = PostData(listOf(measure))
            Log.d("data", "$postData")

            postDataToServer(postData)
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

    fun enableGPS(): Boolean {
        var connectionStatus = false
        val locationRequest = LocationRequest.create()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { response ->
            val states = response.locationSettingsStates
            if (states!!.isLocationPresent) {
                connectionStatus = true
            }
        }

        // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {

                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        this,
                        2
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
        return connectionStatus
    }

    private fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Calendar.getInstance().time)
    }

    private fun postDataToServer(postData: PostData) {

        val jsonPayload = Gson().toJson(postData)
        Log.d("RetrofitPayload", jsonPayload)

        val call = retrofitInterface().postData(postData)
        call.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    Log.d("Retrofit", "Data posted successfully")
                } else {
                    Log.d("Retrofit", "Failed to post data")
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.d("Retrofit", "Error: " + t.message)
            }
        })
    }
}
