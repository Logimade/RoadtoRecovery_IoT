package com.example.tudoem1

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.postDelayed
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.tudoem1.NetMonsterActivity.Companion.REFRESH_RATIO
import com.example.tudoem1.databaseUtils.DatabasePrototype
import com.example.tudoem1.databaseUtils.MeasureStructure
import com.example.tudoem1.databaseUtils.MetricStructure
import com.example.tudoem1.services.NetMonsterService
import com.example.tudoem1.webservices.Coordinates
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
import com.google.gson.GsonBuilder
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.core.feature.detect.DetectorHspaDc
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedCellInfo
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedNrServiceState
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedPhysicalChannel
import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.nr.NrNsaState
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

/**
 * Activity periodically updates data (once in [REFRESH_RATIO] ms) when it's on foreground.
 */
class NetMonsterActivity : AppCompatActivity() {

    companion object {
        private const val REFRESH_RATIO = 1_000L
    }

    private val db by lazy {
        DatabasePrototype.getDatabase(
            this
        )
    }

    private lateinit var measureId: UUID
    private val handler = Handler(Looper.getMainLooper())
    private val adapter = MainAdapter()
    private lateinit var recyclerView: RecyclerView


    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NetMonsterService.ACTION_DATA_UPDATED) {
                val metricsJson = intent.getStringExtra(NetMonsterService.EXTRA_METRICS)
                Log.d("MainActivity", "Received metrics: $metricsJson")
                // Update your UI or perform any necessary actions with the received data
                adapter.data = NetMonsterService.merged
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_netmonster)

        recyclerView = findViewById(R.id.recycler)
        recyclerView.adapter = adapter

//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowTitleEnabled(false)
//
//        toolbar.setNavigationOnClickListener {
//            val intent = Intent(
//                this@NetMonsterActivity,
//                MainActivity::class.java
//            )
//            startActivity(intent)
//            finish()
//        }

//        measureId = UUID.randomUUID()
//        lifecycleScope.launch {
//            db.daoNetworkMethods().insertMeasure(
//                measure = MeasureStructure(
//                    id = measureId,
//                    startDate = getCurrentDateTime(),
//                    coordinatesStart = locationCoordinates
//                )
//            )
//        }

        val filter = IntentFilter(NetMonsterService.ACTION_DATA_UPDATED)
        registerReceiver(dataReceiver, filter, RECEIVER_EXPORTED)

        startNetMonsterService()
    }

    private fun startNetMonsterService() {
        val startIntent = Intent(this, NetMonsterService::class.java).apply {
            action = NetMonsterService.ACTION_START_SERVICE
        }
        startService(startIntent)
    }

    private val gpsSwitchStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action) {
                val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (!isGpsEnabled) {
                    enableGPS()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
//        filter.addAction(Intent.ACTION_PROVIDER_CHANGED)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            registerReceiver(gpsSwitchStateReceiver, filter, RECEIVER_EXPORTED)
//        } else {
//            registerReceiver(gpsSwitchStateReceiver, filter, RECEIVER_NOT_EXPORTED)
//        }
//
//
//        if (broadcastReceiver == null) {
//            broadcastReceiver = object : BroadcastReceiver() {
//                override fun onReceive(context: Context, intent: Intent) {
//                    println(intent.extras?.getDouble("Latitude"))
//                    println(intent.extras?.getDouble("Longitude"))
//                    locationCoordinates.lat = intent.extras?.getDouble("Latitude")!!
//                    locationCoordinates.long = intent.extras?.getDouble("Longitude")!!
//                }
//            }
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            registerReceiver(broadcastReceiver, IntentFilter("location_update"), RECEIVER_EXPORTED)
//        } else {
//            registerReceiver(broadcastReceiver, IntentFilter("location_update"))
//
//        }

//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_PHONE_STATE
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            loop()
//        } else requestPermissions(
//            arrayOf(
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.READ_PHONE_STATE
//            ), 0
//        )

    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
//        lifecycleScope.launch {
//            db.daoNetworkMethods().updateMeasure(
//                key = measureId,
//                endMeasure = getCurrentDateTime(),
//                coordinatesStopped = locationCoordinates
//            )
//        }
    }

    private fun loop() {
        updateData()
        handler.postDelayed(REFRESH_RATIO) { loop() }
    }

    @SuppressLint("MissingPermission")
    private fun updateData() {
//        Log.d("Coordinates", "$locationCoordinates")
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
//            lifecycleScope.launch {
//                db.daoNetworkMethods().insertMetric(
//                    metric =
//                    MetricStructure(
//                        timeStamp = getCurrentDateTime(),
//                        measureId = measureId,
//                        coordinates = locationCoordinates,
//                        metrics = merged.joinToString(separator = "\n")
//                    )
//                )
//            }

//            val metrics = merged.joinToString(separator = "\n")
//            val coordinates = Coordinates(locationCoordinates.lat, locationCoordinates.long)
//            val measure = Measure(getCurrentDateTime(), coordinates, metrics)
//            val postData = PostData(listOf(measure))
//            Log.d("data", "$postData")

//            postDataToServer(postData)
//            Log.d("NTM-Subset", " \n${subset.joinToString(separator = "\n")}")
            // All detectors that are bundled in NetMonster Core
            val networkType: NetworkType = getNetworkType(0)
            Log.d("NetworkType", " $networkType")
            // Only HSPA+42 (guess, not from RIL)
            val isHspaDc: NetworkType? = getNetworkType(0, DetectorHspaDc())
            Log.d("isHspaDc", " $isHspaDc")
            // LTE-A from CellInfo (guess, not from RIL), NSA NR
            val isLteCaCellInfo: NetworkType? = getNetworkType(0, DetectorLteAdvancedCellInfo())
            Log.d("isLteCaCellInfo", " $isLteCaCellInfo")
            // LTE-A from ServiceState (from RIL, Android P+)
            val isLteCaServiceState: NetworkType? =
                getNetworkType(0, DetectorLteAdvancedNrServiceState())
            Log.d("isLteCaServiceState", " $isLteCaServiceState")

            // LTE-A from PhysicalChannel (from RIL, Android P+)
            val isLteCaPhysicalChannel: NetworkType? =
                getNetworkType(0, DetectorLteAdvancedPhysicalChannel())
            Log.d("isLteCaPhysicalChannel", " $isLteCaPhysicalChannel")

            // LTE-A and NR from DisplayInfo (marketing purposes, might result false-positive data, Android R+)
            // You can also detect only LTE-A or NR using one of classes:
            // - DetectorLteAdvancedServiceState ... for LTE-A
            // - DetectorNsaNr ... for NR NSA
            val networkType1: NetworkType =
                NetMonsterFactory.get(this@NetMonsterActivity).getNetworkType(0)
            if (networkType1 is NetworkType.Nr.Nsa) {
                val state: NrNsaState =
                    networkType1.nrNsaState // For more info refer to NrNsaState class
                Log.d("NetworkType1", " $state")
            }


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
