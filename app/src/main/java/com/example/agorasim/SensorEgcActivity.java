package com.example.agorasim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;

import util.TimeOut;
import visiomed.fr.bleframework.common.BLECenter;
import visiomed.fr.bleframework.device.BloodPressureMonitor;

public class SensorEgcActivity extends AppCompatActivity {

    int tickCount = 0;
    private String mac;
    private BLECenter bleCenter;
    private BloodPressureMonitor bpm;
    private TimeOut timeOut;
    private TextView titulo, txt_data_hora, txt_egc, txtEstado;
    private Button btnSair, btnVoltar;
    private SensorEgcActivity self;
    private ProgressBar progressBar;
    private Button btnSeguinte;
    private int heart_rate;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_egc);
    }

    public void back_menu(View view) {
        Intent in = new Intent(SensorEgcActivity.this, Menu_escolhas.class);
        startActivity(in);
    }
    /**/
}