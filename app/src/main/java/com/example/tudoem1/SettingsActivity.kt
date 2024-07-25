package com.example.tudoem1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import util.Util

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnBackSettings: Button
    private lateinit var btnSetIp: Button
    private lateinit var ipTextInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        Util.fullscreen(this)

        btnBackSettings = findViewById(R.id.btnBackSettings)
        btnSetIp = findViewById(R.id.btnSetIp)
        ipTextInput = findViewById(R.id.ip_text_input)

        btnBackSettings.setOnClickListener {
            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnSetIp.setOnClickListener {
            val ip = ipTextInput.text.toString()
            Log.d("ip strinf", ip)
            val intent = Intent(this@SettingsActivity, AllMeasuresActivity::class.java)
            intent.putExtra("Ip_text", ip)
            startActivity(intent)
            finish()
        }
    }
}
