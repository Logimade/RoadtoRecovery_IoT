package com.example.tudoem1

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import util.TimeOut
import visiomed.fr.bleframework.common.BLECenter
import visiomed.fr.bleframework.device.BloodPressureMonitor
import visiomed.fr.bleframework.device.Oximeter
import visiomed.fr.bleframework.device.Thermometer
import visiomed.fr.bleframework.device.ecg.ECG

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
    private lateinit var thermometerState : TextView
    private lateinit var valorSYS: TextView
    private lateinit var valorDIA: TextView
    private lateinit var txtEstado_Tensio: TextView
    private lateinit var oximeterSpOValue: TextView
    private lateinit var oximeterPrBpmValue: TextView
    private lateinit var oximeterState: TextView
    private lateinit var homeButton: Button

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
        oximeterState = view.findViewById(R.id.txtEstado_Oxy) }
}
