package com.example.tudoem1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.squareup.otto.Subscribe
import visiomed.fr.bleframework.common.BLECenter
import visiomed.fr.bleframework.common.BLEContext
import visiomed.fr.bleframework.data.ecg.ECGExamData
import visiomed.fr.bleframework.data.ecg.ECGRealTimeData
import visiomed.fr.bleframework.device.BloodPressureMonitor
import visiomed.fr.bleframework.device.DeviceFactory
import visiomed.fr.bleframework.device.Oximeter
import visiomed.fr.bleframework.device.Thermometer
import visiomed.fr.bleframework.device.ecg.ECG
import visiomed.fr.bleframework.event.common.BLEDeviceStateEvent
import visiomed.fr.bleframework.event.oximeter.OximeterEvent
import visiomed.fr.bleframework.event.thermometer.ThermometerEvent

class SensorFragment : Fragment() {

    private var tickCount = 0
    private var temperatura = 0.0F
    private var oxygenContent = -1
    private var pulse = -1
    private var sys = 0
    private var dia = 0
    private var HR = 0
    private var flagTemp = 0
    private var ip1: String? = null

    private lateinit var bleCenter: BLECenter
    private lateinit var thermometer: Thermometer
    private lateinit var bpm: BloodPressureMonitor
    private lateinit var oximeter: Oximeter
    private lateinit var ecg: ECG

    private lateinit var thermometerValue: TextView
    private lateinit var thermometerState: TextView
    private lateinit var valorSYS: TextView
    private lateinit var valorDIA: TextView
    private lateinit var txtEstado_Tensio: TextView
    private lateinit var oximeterSpOValue: TextView
    private lateinit var oximeterPrBpmValue: TextView
    private lateinit var oximeterState: TextView
    private lateinit var homeButton: Button

    private val  ecgClass = ECGExamData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_medicoes, container, false)
        initViews(view)

        homeButton = view.findViewById(R.id.btnVoltar)
        homeButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
        return view

    }

    private fun initViews(view: View) {
        thermometerValue = view.findViewById(R.id.txt_thermo)
        thermometerState = view.findViewById(R.id.txtEstado_Thermo)
        valorSYS = view.findViewById(R.id.valorSYS)
        valorDIA = view.findViewById(R.id.valorDIA)
        txtEstado_Tensio = view.findViewById(R.id.txtEstado_Tensio)
        oximeterSpOValue = view.findViewById(R.id.valorSpO)
        oximeterPrBpmValue = view.findViewById(R.id.valorPrbpm)
        oximeterState = view.findViewById(R.id.txtEstado_Oxy)
    }

    override fun onStart() {
        super.onStart()
        BLECenter.DEBUG_LOG_ON = true
        BLECenter.DEBUG_LOG_LEVEL = 1
        bleCenter = BLEContext.getBLECenter(requireContext().applicationContext)

//        startLEScan_Thermo()
//        startLEScan_Oxy()
//        startLEScan_ECG()
        BLECenter.bus().register(this)
    }

    override fun onResume() {
        super.onResume()

        if (::thermometer.isInitialized) {
            thermometerState.text = if (thermometer.hasConnection()) {
                "Sensor Connected"
            } else {

                "Searching for sensor"
            }
        }

//        showLoading_Thermo()

        txtEstado_Tensio.text = "Searching for sensor"
//        showLoading_Tensio()

//        txtEstado_Ecg.text = "Searching for sensor"
//        showLoading_Ecg()
        if (::oximeter.isInitialized) {
            oximeterState.text = if (oximeter.hasConnection()) {
                "Sensor Connected"
            } else {
                "Searching for sensor"
            }
        }
//        showLoading_Oxy()
        val data = ecgClass.heartRate
        data.toString().let { Log.d("test", it) }
    }

    private fun startLEScan_Thermo() {
        bleCenter.startBLEScan(DeviceFactory.Device.THERMOMETER.scanOption)

        Thread {
            var check = true
            while (check) {
                val devices = bleCenter.devices
                if (devices.isNotEmpty()) {
                    devices.forEach { device ->
                        if (device is Thermometer) {
                            device.connect()
                            thermometer =
                                bleCenter.getDevice(device.bleDevice.address) as Thermometer
                            val mac = device.bleDevice.address
                            Log.i("LOG_APP", mac ?: "")

                            if (ActivityCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                // Request permissions if needed
                                ActivityCompat.requestPermissions(
                                    requireActivity(),
                                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                                    0
                                )
                                return@Thread
                            }

                            Log.i("LOG_APP", device.bleDevice.name ?: "")
                            stopLEScan_Thermo()
                            check = false
                        }
                    }
                }
            }
            Thread.interrupted()
        }.start()
    }

    private fun stopLEScan_Thermo() {
        bleCenter.stopBLEScan()
    }

    @Subscribe
    @Suppress("unused")
    fun onThermometerEvent(event: ThermometerEvent) {

        val data = event.thermometerData
        temperatura = data.temperature / 10.0F
        Log.d("MYINT", "value1: $temperatura")

        if (temperatura < 32 || temperatura > 43) {
            flagTemp = 1
            temperatura = 1F
//            txtEstado_Thermo.text = "Searching for sensor"
//            startLEScan_Thermo()
        } else {
            thermometerValue.text = temperatura.toString()
        }
    }

    private fun startLEScan_Oxy() {
        bleCenter.startBLEScan(DeviceFactory.Device.OXIMETER.scanOption)

        Thread {
            var check = true
            while (check) {
                Log.i("BLE Oxy", bleCenter.devices.toString())

                val devices = bleCenter.devices
                if (devices.isNotEmpty()) {
                    devices.forEach { device ->
                        if (device is Oximeter) {
                            device.connect()
                            oximeter = bleCenter.getDevice(device.bleDevice.address) as Oximeter
                            val mac = device.bleDevice.address
                            Log.i("LOG_APP1", mac)

                            if (ActivityCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(
                                    requireActivity(),
                                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                                    0
                                )
                                return@Thread
                            }

                            Log.i("LOG_APP", device.bleDevice.name ?: "")
                            stopLEScan_Oxy()
                            check = false
                        }
                    }
                }
            }
            if (!check) {
                Thread.currentThread().interrupt()
            }
        }.start()
    }

    private fun stopLEScan_Oxy() {
        bleCenter.stopBLEScan()
    }

    @Subscribe
    @Suppress("unused")
    fun onOximeterEvent(event: OximeterEvent) {

        val oxygenContent = event.oximeterData.oxygenContent
        val pulse = event.oximeterData.pulse
        Log.d("Oxy Values", oxygenContent.toString())
        Log.d("Oxy Values", pulse.toString())


        oximeterSpOValue.text = oxygenContent.toString()
        oximeterPrBpmValue.text = pulse.toString()
    }

    @Subscribe
    @Suppress("unused")
    fun onBLEDeviceConnectionStateEvent_Oxy(event: BLEDeviceStateEvent) {

        if (event.connectionState == BLEDeviceStateEvent.CONNECTION_STATE_CONNECTING) {
            oximeterState.setText(R.string.state_connecting)
        } else if (event.connectionState == BLEDeviceStateEvent.CONNECTION_STATE_CONNECTED) {
            oximeterState.setText("A realizar medição ...")
        } else if (event.connectionState == BLEDeviceStateEvent.CONNECTION_STATE_DISCONNECTING) {
            oximeterState.setText(R.string.state_disconnecting)
        } else if (event.connectionState == BLEDeviceStateEvent.CONNECTION_STATE_DISCONNECTED) {
            // txtEstado.setText(R.string.action_connect);
        } else if (event.connectionState == BLEDeviceStateEvent.CONNECTION_STATE_GATT_FAILED) {
            oximeterState.setText("Dispositivo desligado")
            if (pulse < 0 || oxygenContent < 0) {
                startLEScan_Oxy()
                oximeterState.setText("Searching for sensor")
            }
        }
    }

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
