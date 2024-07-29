package com.example.tudoem1.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.tudoem1.databaseUtils.DatabasePrototype
import com.example.tudoem1.databaseUtils.MeasureStructure
import com.example.tudoem1.databaseUtils.MetricStructure
import com.example.tudoem1.webservices.Coordinates
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.core.feature.detect.DetectorHspaDc
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedCellInfo
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedNrDisplayInfo
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedNrServiceState
import cz.mroczis.netmonster.core.feature.detect.DetectorLteAdvancedPhysicalChannel
import cz.mroczis.netmonster.core.model.cell.ICell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NetMonsterService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO) // Or another appropriate dispatcher

    private lateinit var db: DatabasePrototype
    private lateinit var handler: Handler
    private lateinit var measureId: UUID
    private var locationCoordinates = Coordinates(0.0, 0.0)
    private var isServiceRunning = false

    private val locationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.action == "location_update") {
                    val latitude = it.getDoubleExtra("Latitude", 0.0)
                    val longitude = it.getDoubleExtra("Longitude", 0.0)
                    locationCoordinates.lat = latitude
                    locationCoordinates.long = longitude
                    Log.d("NetMonsterService", "Location updated: $locationCoordinates")
                }
            }
        }
    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        db = DatabasePrototype.getDatabase(this)
        handler = Handler(Looper.getMainLooper())
        isServiceRunning = false
    }

    private fun startUpdatingData() {
        handler.postDelayed({
            updateData()
            if (isServiceRunning) {
                startUpdatingData()
            }
        }, REFRESH_INTERVAL_MS)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                if (!isServiceRunning) {
                    registerReceiver(locationReceiver, IntentFilter("location_update"),
                        RECEIVER_NOT_EXPORTED
                   )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        registerReceiver(locationReceiver, IntentFilter("location_update"), RECEIVER_EXPORTED)
                    } else {
                        registerReceiver(locationReceiver, IntentFilter("location_update"))

                    }

                    measureId = UUID.randomUUID()

                    serviceScope.launch {
                        db.daoNetworkMethods().insertMeasure(
                            measure = MeasureStructure(
                                id = measureId,
                                startDate = getCurrentDateTime(),
                                coordinatesStart = locationCoordinates
                            )
                        )
                    }
                    startService()
                    isServiceRunning = true
                }
            }

            ACTION_STOP_SERVICE -> {
                if (isServiceRunning) {
                    stopService()
                    isServiceRunning = false
                }
            }

            else -> {
                Log.d("NetMonsterService", "Unknown action received: ${intent?.action}")
            }
        }
        return START_STICKY
    }

    private fun startService() {
        // Check permissions before starting any operation
        if (arePermissionsGranted()) {
            // Perform initialization or any tasks you need to start the service
            // Example: Starting periodic tasks, registering listeners, etc.
            Log.d("NetMonsterService", "Service started")
            startUpdatingData()
        } else {
            Log.e("NetMonsterService", "Permissions not granted to start service")
            // Handle case where permissions are not granted (e.g., inform the user)
        }
    }


    private fun stopService() {
        // Clean up tasks, unregister receivers, stop updates, etc.
        Log.d("NetMonsterService", "Service stopped")
        handler.removeCallbacksAndMessages(null)
        db.daoNetworkMethods().updateMeasure(
            key = measureId, endMeasure = getCurrentDateTime(),
            coordinatesStopped = locationCoordinates
        )
        Log.d("NetMonsterService", "Database Updated")
//        serviceScope.launch {
//            db.daoNetworkMethods().updateMeasure(
//                key = measureId, endMeasure = getCurrentDateTime(),
//                coordinatesStopped = locationCoordinates
//            )
//        }
        stopDataUpdates()
        stopSelf() // Stop the service itself
    }

    private fun stopDataUpdates() {
        // Example: Cancel any ongoing coroutines or stop any ongoing operations
        unregisterReceiver(locationReceiver)
        serviceScope.cancel()

    }


    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    @SuppressLint("MissingPermission")
    private fun updateData() {
        val SUBSCRIPTION_ID = 1
        NetMonsterFactory.get(this).apply {

            val networkType: NetworkType = getNetworkType(SUBSCRIPTION_ID)
            Log.d("Network Type", "$networkType")
            // Only HSPA+42 (guess, not from RIL)
            val isHspaDc: NetworkType? = getNetworkType(SUBSCRIPTION_ID, DetectorHspaDc())
            Log.d("Network isHspaDc", "$isHspaDc")

            // LTE-A from CellInfo (guess, not from RIL), NSA NR
            val isLteCaCellInfo: NetworkType? =
                getNetworkType(SUBSCRIPTION_ID, DetectorLteAdvancedCellInfo())
            Log.d("Network isLteCaCellInfo", "$isLteCaCellInfo")

            // LTE-A from ServiceState (from RIL, Android P+)
            val isLteCaServiceState: NetworkType? =
                getNetworkType(SUBSCRIPTION_ID, DetectorLteAdvancedNrServiceState())
            Log.d("Network isLteCaServiceState", "$isLteCaServiceState")

            // LTE-A from PhysicalChannel (from RIL, Android P+)
            val isLteCaPhysicalChannel: NetworkType? =
                getNetworkType(SUBSCRIPTION_ID, DetectorLteAdvancedPhysicalChannel())
            Log.d("Network isLteCaPhysicalChannel", "$isLteCaPhysicalChannel")

            // LTE-A and NR from DisplayInfo (marketing purposes, might result false-positive data, Android R+)
            // You can also detect only LTE-A or NR using one of classes:
            // - DetectorLteAdvancedServiceState ... for LTE-A
            // - DetectorNsaNr ... for NR NSA
            val isLteCaOrNsaNrDisplayInfo: NetworkType? =
                getNetworkType(SUBSCRIPTION_ID, DetectorLteAdvancedNrDisplayInfo())
            Log.d("Network isLteCaOrNsaNrDisplayInfo", "$isLteCaOrNsaNrDisplayInfo")




            merged = getCells()
            Log.d("NetMonsterService", "Data updated: \n${merged.joinToString(separator = "\n")}")
            serviceScope.launch {
                Log.d("Database Store", "Is storing")
                db.daoNetworkMethods().insertMetric(
                    MetricStructure(
                        timeStamp = getCurrentDateTime(),
                        measureId = measureId,
                        coordinates = locationCoordinates,
                        metrics = merged.joinToString(separator = "\n"),
                        networkType = networkType.toString(),
                        isHspaDc = isHspaDc?.toString(),
                        isLteCaCellInfo = isLteCaCellInfo?.toString(),
                        isLteCaServiceState = isLteCaServiceState?.toString(),
                        isLteCaPhysicalChannel = isLteCaPhysicalChannel?.toString(),
                        isLteCaOrNsaNrDisplayInfo = isLteCaOrNsaNrDisplayInfo?.toString()
                    )
                )
            }

            val intent = Intent(ACTION_DATA_UPDATED).apply {
                putExtra(EXTRA_METRICS, merged.joinToString(separator = "\n"))
            }
            sendBroadcast(intent)
        }
    }

    // Permission handling
    private fun arePermissionsGranted(): Boolean {
        for (permission in LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }


    private fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Calendar.getInstance().time)
    }

    companion object {
        const val ACTION_START_SERVICE = "com.example.tudoem1.action.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.tudoem1.action.STOP_SERVICE"
        private const val REFRESH_INTERVAL_MS = 1000L // Example refresh interval
        const val ACTION_DATA_UPDATED = "com.example.tudoem1.action.DATA_UPDATED"
        const val EXTRA_METRICS = "extra_metrics"
        lateinit var merged: List<ICell>
        private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        )
    }
}
