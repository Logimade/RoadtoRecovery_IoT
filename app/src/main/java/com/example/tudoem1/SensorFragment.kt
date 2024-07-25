package com.example.tudoem1

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import visiomed.fr.bleframework.common.BLECenter
import visiomed.fr.bleframework.data.ecg.ECGExamData
import visiomed.fr.bleframework.device.Oximeter
import visiomed.fr.bleframework.device.Thermometer

class SensorFragment : Fragment() {

    private lateinit var thermometerValue: TextView
    private lateinit var oximeterSpOValue: TextView
    private lateinit var oximeterPrBpmValue: TextView
    private lateinit var homeButton: Button
    private lateinit var readTemperature: Button
    private lateinit var readOxygen: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_medicoes, container, false)
        initViews(view)

        val socketIP = activity?.intent?.getStringExtra("Ip_text")
        homeButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        readTemperature.setOnClickListener {
            val oxygenContent = activity?.intent?.getIntExtra("OXYGEN_CONTENT", -1) ?: -1
            val pulse = activity?.intent?.getIntExtra("PULSE", -1) ?: -1
            val intent = Intent(activity, Activity_Temperature::class.java).apply {
                putExtra("OXYGEN_CONTENT", oxygenContent)
                putExtra("PULSE", pulse)
                putExtra("Ip_text", socketIP)
            }
            startActivity(intent)
        }

        readOxygen.setOnClickListener {
            val temperature = activity?.intent?.getFloatExtra("Temperature", -1.0F)
            val intent = Intent(activity, Activity_Oxy::class.java).apply {
                putExtra("Temperature", temperature)
                putExtra("Ip_text", socketIP)
            }
            startActivity(intent)
        }

        val oxygenContent = activity?.intent?.getIntExtra("OXYGEN_CONTENT", -1) ?: -1
        val pulse = activity?.intent?.getIntExtra("PULSE", -1) ?: -1

        if (oxygenContent != -1 && pulse != -1) {
            oximeterSpOValue.text = oxygenContent.toString()
            oximeterPrBpmValue.text = pulse.toString()
        } else {
            oximeterSpOValue.text = "No Data"
            oximeterPrBpmValue.text = "No Data"
        }

        val temperature = activity?.intent?.getFloatExtra("Temperature", -1.0F)

        // Display the temperature value
        if (temperature != -1.0F) {
            thermometerValue.text = "$temperature°C"
        } else {
            thermometerValue.text = "No Data"
        }

        return view

    }

    private fun initViews(view: View) {
        thermometerValue = view.findViewById(R.id.txt_thermo)
        oximeterSpOValue = view.findViewById(R.id.valorSpO)
        oximeterPrBpmValue = view.findViewById(R.id.valorPrbpm)

        homeButton = view.findViewById(R.id.btnVoltar)
        readTemperature = view.findViewById(R.id.btnStartReadingTemperature)
        readOxygen = view.findViewById(R.id.btnStartReadingOxygen)
    }


    override fun onStart() {
        super.onStart()
//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)
//        BLECenter.DEBUG_LOG_ON = true
//        BLECenter.DEBUG_LOG_LEVEL = 1
//        bleCenter = BLEContext.getBLECenter(requireContext().applicationContext)

    }
//
//    override fun onStop() {
//        super.onStop()
//        BLECenter.bus().unregister(this)
//    }
//    override fun onResume() {
//        super.onResume()
//
//        if (::thermometer.isInitialized) {
//            thermometerState.text = if (thermometer.hasConnection()) {
//                "Sensor Connected"
//            } else {
//
//                "Searching for sensor"
//            }
//        }
//
////        showLoading_Thermo()
//
//        txtEstado_Tensio.text = "Searching for sensor"
////        showLoading_Tensio()
//
////        txtEstado_Ecg.text = "Searching for sensor"
////        showLoading_Ecg()
//        if (::oximeter.isInitialized) {
//            oximeterState.text = if (oximeter.hasConnection()) {
//                "Sensor Connected"
//            } else {
//                "Searching for sensor"
//            }
//        }
////        showLoading_Oxy()
//        val data = ecgClass.heartRate
//        data.toString().let { Log.d("test", it) }
//    }

//    private fun startLEScan_Thermo() {
//        Log.i("enter here", "scanning")
//        bleCenter.startBLEScan(DeviceFactory.Device.THERMOMETER.scanOption)
//
//        Thread {
//            while (true) {
//                val devices = bleCenter.devices
//                if (devices.isNotEmpty()) {
//                    devices.forEach { device ->
//                        if (device is Thermometer) {
//                            device.connect()
//                            thermometer =
//                                bleCenter.getDevice(device.bleDevice.address) as Thermometer
//                            mac = device.bleDevice.address
//                            Log.i("LOG_APP", mac ?: "")
//
//                            if (ActivityCompat.checkSelfPermission(
//                                    requireContext(),
//                                    Manifest.permission.BLUETOOTH_CONNECT
//                                ) != PackageManager.PERMISSION_GRANTED
//                            ) {
//                                // Request permissions if needed
//                                ActivityCompat.requestPermissions(
//                                    requireActivity(),
//                                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
//                                    0
//                                )
//                                return@Thread
//                            }
//
//                            Log.i("LOG_APP", device.bleDevice.name ?: "")
//                            stopLEScan_Thermo()
//                            return@Thread
//                        }
//                    }
//                }
//            }
//        }.start()
//    }
//
//    private fun stopLEScan_Thermo() {
//        BLECenter.bus().register(thermometer)
//        BLECenter.bus().post(thermometer)
//        bleCenter.stopBLEScan()
//    }
//
//    @Subscribe
//    fun onThermometerEvent(event: ThermometerEvent) {
//
//        val data = event.thermometerData
//        temperatura = data.temperature / 10.0F
//        Log.d("MYINT", "value1: $temperatura")
//
//        if (temperatura < 32 || temperatura > 43) {
//            flagTemp = 1
//            temperatura = 1F
////            txtEstado_Thermo.text = "Searching for sensor"
////            startLEScan_Thermo()
//        } else {
//            thermometerValue.text = temperatura.toString()
//            thermometer.disconnect()
//        }
//    }
//
//    private fun startLEScan_Oxy() {
//        bleCenter.startBLEScan(DeviceFactory.Device.OXIMETER.scanOption)
//
//        Thread {
//            while (true) {
//                Log.i("BLE Oxy", bleCenter.devices.toString())
//
//                val devices = bleCenter.devices
//                if (devices.isNotEmpty()) {
//                    devices.forEach { device ->
//                        if (device is Oximeter) {
//                            device.connect()
//                            oximeter = bleCenter.getDevice(device.bleDevice.address) as Oximeter
//                            mac = device.bleDevice.address
//
//                            Log.i("LOG_APP1", mac)
//
//                            if (ActivityCompat.checkSelfPermission(
//                                    requireContext(),
//                                    Manifest.permission.BLUETOOTH_CONNECT
//                                ) != PackageManager.PERMISSION_GRANTED
//                            ) {
//                                ActivityCompat.requestPermissions(
//                                    requireActivity(),
//                                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
//                                    0
//                                )
//                                return@Thread
//                            }
//
//                            Log.i("LOG_APP", device.bleDevice.name ?: "")
//                            stopLEScan_Oxy()
//                            return@Thread
//                        }
//                    }
//                }
//            }
//        }.start()
//    }
//
//    private fun stopLEScan_Oxy() {
//        BLECenter.bus().register(oximeter)
//        bleCenter.stopBLEScan()
//    }
//
//
//    @Subscribe
//    fun onOximeterEvent(event: OximeterEvent) {
//
//        val oxygenContent = event.oximeterData.oxygenContent
//        val pulse = event.oximeterData.pulse
//        Log.d("Oxy Values", oxygenContent.toString())
//        Log.d("Oxy Values", pulse.toString())
//
//
//        oximeterSpOValue.text = oxygenContent.toString()
//        oximeterPrBpmValue.text = pulse.toString()
//
//        oximeter.disconnect()
//        BLECenter.bus().unregister(oximeter)
//    }

    //    @Subscribe
//    fun onBLEDeviceConnectionStateEvent_Oxy(event: BLEDeviceStateEvent) {
//
//        if (event.connectionState == BLEDeviceStateEvent.CONNECTION_STATE_CONNECTING) {
//            oximeterState.setText(R.string.state_connecting)
//        } else if (event.connectionState == BLEDeviceStateEvent.CONNECTION_STATE_CONNECTED) {
//            oximeterState.setText("A realizar medição ...")
//        } else if (event.connectionState == BLEDeviceStateEvent.CONNECTION_STATE_DISCONNECTING) {
//            oximeterState.setText(R.string.state_disconnecting)
//        } else if (event.connectionState == BLEDeviceStateEvent.CONNECTION_STATE_DISCONNECTED) {
//            // txtEstado.setText("R.string.action_connect");
//        } else if (event.connectionState == BLEDeviceStateEvent.CONNECTION_STATE_GATT_FAILED) {
//            oximeterState.setText("Dispositivo desligado")
//            if (pulse < 0 || oxygenContent < 0) {
//                startLEScan_Oxy()
//                oximeterState.setText("Searching for sensor")
//            }
//        }
//    }

//    private fun startLEScan_ECG() {
//        bleCenter.startBLEScan(DeviceFactory.Device.ECG.scanOption)
//
//        Thread {
//            var check = true
//            while (check) {
//                Log.i("BLE Oxy", bleCenter.devices.toString())
//
//                val devices = bleCenter.devices
//                if (devices.isNotEmpty()) {
//                    devices.forEach { device ->
//                        if (device is ECG) {
//                            device.connect()
//                            ecg = bleCenter.getDevice(device.bleDevice.address) as ECG
//                            val mac = device.bleDevice.address
//                            Log.i("LOG_APP1", mac)
////device.startRealTimeMode()
////ecg.startRealTimeMode()
//                            Log.d("ress", ecg.getRecordAtIndex(0).toString())
//                            if (ActivityCompat.checkSelfPermission(
//                                    requireContext(),
//                                    Manifest.permission.BLUETOOTH_CONNECT
//                                ) != PackageManager.PERMISSION_GRANTED
//                            ) {
//                                ActivityCompat.requestPermissions(
//                                    requireActivity(),
//                                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
//                                    0
//                                )
//                                return@Thread
//                            }
//
//                            Log.i("LOG_APP", device.bleDevice.name ?: "")
//                            stopLEScan_ECG()
//                            check = false
//                        }
//                    }
//                }
//            }
//            if (!check) {
//                Thread.currentThread().interrupt()
//            }
//        }.start()
//    }
//
//    private fun stopLEScan_ECG() {
//        bleCenter.stopBLEScan()
//    }


}
