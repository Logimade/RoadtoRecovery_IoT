package com.example.tudoem1.gpsUtils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class GPService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                Log.d(TAG, "onLocationResult: $location")
                val i = Intent("location_update")
                i.putExtra("Latitude", location.latitude)
                i.putExtra("Longitude", location.longitude)
                sendBroadcast(i)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = createLocationRequest()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkSettingsAndStartLocationUpdates();
        }
    }

    private fun checkSettingsAndStartLocationUpdates() {
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest).build()
        val client = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask: Task<LocationSettingsResponse> =
            client.checkLocationSettings(request)
        locationSettingsResponseTask.addOnSuccessListener { //Settings of device are satisfied and we can start location updates
            startLocationUpdates()
        }
        locationSettingsResponseTask.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    Log.e(TAG, "${e.message}")
                } catch (ex: SendIntentException) {
                    ex.printStackTrace()
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    private fun createLocationRequest(): LocationRequest {

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 500)
            .setWaitForAccurateLocation(false)
            .build()

        return locationRequest
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }
}