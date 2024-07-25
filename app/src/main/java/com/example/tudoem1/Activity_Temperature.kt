package com.example.tudoem1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.squareup.otto.Subscribe
import visiomed.fr.bleframework.common.BLECenter
import visiomed.fr.bleframework.common.BLEContext
import visiomed.fr.bleframework.device.DeviceFactory
import visiomed.fr.bleframework.device.Oximeter
import visiomed.fr.bleframework.device.Thermometer
import visiomed.fr.bleframework.event.thermometer.ThermometerEvent
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.nio.charset.StandardCharsets

class Activity_Temperature : AppCompatActivity() {

    private lateinit var mac: String

    private lateinit var bleCenter: BLECenter
    private lateinit var thermometer: Thermometer
    private lateinit var sensorStatus: TextView
    private lateinit var loadingBar: ProgressBar
    private var ip: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperature)

        sensorStatus = findViewById(R.id.sensorStatus)
        loadingBar = findViewById(R.id.loadingBar)

        ip = intent?.getStringExtra("Ip_text")
    }

    override fun onStart() {
        super.onStart()
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        BLECenter.DEBUG_LOG_ON = true
        BLECenter.DEBUG_LOG_LEVEL = 1
        bleCenter = BLEContext.getBLECenter(this@Activity_Temperature)
        BLECenter.bus().register(this@Activity_Temperature)

        startLEScan_Thermo()

    }

    override fun onPause() {
        super.onPause()
        BLECenter.bus().unregister(this@Activity_Temperature)
    }

    @SuppressLint("MissingPermission")
    private fun startLEScan_Thermo() {

        sensorStatus.text = "Reading Sensor"
        sensorStatus.visibility = View.VISIBLE
        loadingBar.visibility = View.VISIBLE


        Log.i("enter here", "scanning")
        bleCenter.startBLEScan(DeviceFactory.Device.THERMOMETER.scanOption)

        Thread {
            while (true) {
                val devices = bleCenter.devices
                if (devices.isNotEmpty()) {
                    devices.forEach { device ->
                        if (device is Thermometer) {
                            device.connect()
                            thermometer =
                                bleCenter.getDevice(device.bleDevice.address) as Thermometer
                            mac = device.bleDevice.address
                            Log.i("LOG_APP", mac ?: "")

                            Log.i("LOG_APP", device.bleDevice.name ?: "")
                            stopLEScan_Thermo()
                            return@Thread
                        }
                    }
                }
            }
        }.start()
    }

    private fun stopLEScan_Thermo() {
        bleCenter.stopBLEScan()
    }

    @Subscribe
    fun onThermometerEvent(event: ThermometerEvent) {

        val data = event.thermometerData
        val temperatura = data.temperature / 10.0F
        Log.d("MYINT", "value1: $temperatura")

        thermometer.disconnect()
        runOnUiThread {
            sensorStatus.text = "Measurement Complete"
            loadingBar.visibility = View.GONE
        }

        Thread {
            try {
                val msg = "S1: $temperatura"
                val socket = Socket(ip, 12345)
                val dos = DataOutputStream(socket.getOutputStream())

                dos.write(msg.toByteArray(StandardCharsets.UTF_8))
                dos.flush()
                dos.close()
                socket.close()

                showToast("Measurement sent to Socket")

                Test(temperatura)

            } catch (e: IOException) {
                e.printStackTrace()
                // Replace with appropriate context
                showToast("IO Exception occurred")
            } catch (e: InterruptedException) {
                e.printStackTrace()
                // Replace with appropriate context
                showToast("Thread was interrupted")
            }
        }.start()
    }

    private fun showToast(message: String) {
        // Replace 'context' with the appropriate context, such as an Activity or Application context
        Toast.makeText(this@Activity_Temperature, message, Toast.LENGTH_LONG).show()
    }

    private fun Test(temperature: Float) {
        val oxygenContent = intent?.getIntExtra("OXYGEN_CONTENT", -1) ?: -1
        val pulse = intent?.getIntExtra("PULSE", -1) ?: -1
        val intent = Intent(this@Activity_Temperature, AllMeasuresActivity::class.java).apply {
            putExtra("Temperature", temperature)
            putExtra("OXYGEN_CONTENT", oxygenContent)
            putExtra("PULSE", pulse)
            putExtra("Ip_text", ip)
        }
        startActivity(intent)
    }
}