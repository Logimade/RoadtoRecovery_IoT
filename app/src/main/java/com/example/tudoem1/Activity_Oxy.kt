package com.example.tudoem1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.otto.Subscribe
import visiomed.fr.bleframework.common.BLECenter
import visiomed.fr.bleframework.common.BLEContext
import visiomed.fr.bleframework.device.DeviceFactory
import visiomed.fr.bleframework.device.Oximeter
import visiomed.fr.bleframework.event.oximeter.OximeterEvent
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.nio.charset.StandardCharsets

class Activity_Oxy : AppCompatActivity() {

    private lateinit var mac: String

    private lateinit var bleCenter: BLECenter
    private lateinit var oximeter: Oximeter
    private lateinit var sensorStatus: TextView
    private lateinit var loadingBar: ProgressBar
    private var ip: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oxy)

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
        bleCenter = BLEContext.getBLECenter(this@Activity_Oxy)
        BLECenter.bus().register(this@Activity_Oxy)

        startLEScan_Oxy()

    }

    @SuppressLint("MissingPermission")
    private fun startLEScan_Oxy() {
        sensorStatus.text = "Reading Sensor"
        sensorStatus.visibility = View.VISIBLE
        loadingBar.visibility = View.VISIBLE


        bleCenter.startBLEScan(DeviceFactory.Device.OXIMETER.scanOption)

        Thread {
            while (true) {
                Log.i("BLE Oxy", bleCenter.devices.toString())

                val devices = bleCenter.devices
                if (devices.isNotEmpty()) {
                    devices.forEach { device ->
                        if (device is Oximeter) {
                            device.connect()
                            oximeter = bleCenter.getDevice(device.bleDevice.address) as Oximeter
                            mac = device.bleDevice.address

                            Log.i("LOG_APP1", mac)

                            Log.i("LOG_APP", device.bleDevice.name ?: "")
                            stopLEScan_Oxy()
                            return@Thread
                        }
                    }
                }
            }
        }.start()
    }

    private fun stopLEScan_Oxy() {
        bleCenter.stopBLEScan()
    }

    override fun onPause() {
        super.onPause()
        BLECenter.bus().unregister(this@Activity_Oxy)
    }

    @Subscribe
    fun onOximeterEvent(event: OximeterEvent) {

        val oxygenContent = event.oximeterData.oxygenContent
        val pulse = event.oximeterData.pulse
        Log.d("Oxy Values", oxygenContent.toString())
        Log.d("Oxy Values", pulse.toString())

        oximeter.disconnect()

        runOnUiThread {
            sensorStatus.text = "Measurement Complete"
            loadingBar.visibility = View.GONE

            // You can update other UI components with the sensor data here
            // e.g. oximeterSpOValue.text = oxygenContent.toString()
            // e.g. oximeterPrBpmValue.text = pulse.toString()
        }

        Thread {
            try {
                val msg = "S4: $oxygenContent(SPO2) $pulse(PR)"
                val socket = Socket(ip, 12345)
                val dos = DataOutputStream(socket.getOutputStream())

                dos.write(msg.toByteArray(StandardCharsets.UTF_8))
                dos.flush()
                dos.close()
                socket.close()

                runOnUiThread {
                    showToast("Measurement sent to Socket")
                }

                Test(oxygenContent, pulse)

            } catch (e: IOException) {
                e.printStackTrace()
                // Replace with appropriate context
                runOnUiThread {
                    showToast("IO Exception occurred")

                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                // Replace with appropriate context
                runOnUiThread {
                    showToast("Thread was interrupted")

                }
            }
        }.start()
    }


    private fun showToast(message: String) {
        // Replace 'context' with the appropriate context, such as an Activity or Application context
        Toast.makeText(this@Activity_Oxy, message, Toast.LENGTH_LONG).show()
    }

    private fun Test(oxygenContent: Int, pulse: Int) {
        val temperature = intent?.getFloatExtra("Temperature", -1.0F)

        val intent = Intent(this@Activity_Oxy, AllMeasuresActivity::class.java).apply {
            putExtra("OXYGEN_CONTENT", oxygenContent)
            putExtra("PULSE", pulse)
            putExtra("Temperature", temperature)
            putExtra("Ip_text", ip)
        }
        startActivity(intent)
    }
}