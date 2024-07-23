package com.example.tudoem1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tudoem1.databaseUtils.DatabasePrototype
import com.example.tudoem1.services.GPService
import com.example.tudoem1.services.NetMonsterService
import com.example.tudoem1.services.UploadService
import util.Util

class MainActivity : AppCompatActivity() {

    private companion object {
        private const val REQUEST_CODE_BLUETOOTH_SCAN = 101
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Util.fullscreen(this)


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
                ),
                REQUEST_CODE_BLUETOOTH_SCAN
            )

        }

        val intent1 = Intent(this@MainActivity, UploadService::class.java)
        startService(intent1)

        val btnStart: Button = findViewById(R.id.btnStart)
        val btnSettings: Button = findViewById(R.id.btnSettings)
        val btnNetwork: Button = findViewById(R.id.btnNetwork)

//        DatabasePrototype.getDatabase(this@MainActivity).clearAllTables()

        btnStart.setOnClickListener {
            startActivity(Intent(this@MainActivity, AllMeasuresActivity::class.java))
            finish()
        }

        btnNetwork.setOnClickListener {
            if (checkGpsStatus()) {
                val i = Intent(applicationContext, GPService::class.java)
                startService(i)
            }

            val i = Intent(this@MainActivity, NetMonsterService::class.java)
            startService(i)

            startActivity(Intent(this@MainActivity, AllMeasuresActivity::class.java))
            finish()
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_BLUETOOTH_SCAN) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, continue with scan operation
            } else {
                // Permission denied, handle accordingly
            }
        }
    }


    private fun checkGpsStatus(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}
