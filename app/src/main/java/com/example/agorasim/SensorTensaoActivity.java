package com.example.agorasim;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.squareup.otto.Subscribe;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import util.TimeOut;
import util.Util;
import visiomed.fr.bleframework.common.BLECenter;
import visiomed.fr.bleframework.common.BLEContext;
import visiomed.fr.bleframework.data.bpm.BPMData;
import visiomed.fr.bleframework.device.BloodPressureMonitor;
import visiomed.fr.bleframework.device.DeviceFactory;
import visiomed.fr.bleframework.device.GenericDevice;
import visiomed.fr.bleframework.event.bpm.BPMMeasurementEvent;
import visiomed.fr.bleframework.event.bpm.BPMStateEvent;
import visiomed.fr.bleframework.event.common.BLEEvent;

public class SensorTensaoActivity extends AppCompatActivity {


    int tickCount = 0;
    private String mac;
    private BLECenter bleCenter;
    private BloodPressureMonitor bpm;
    private TimeOut timeOut;
    private TextView titulo, txt_data_hora, valorSYS, valorDIA, txtEstado;
    private Button btnSair, btnVoltar;
    private SensorTensaoActivity self;
    private ProgressBar progressBar;
    private Button btnSeguinte;
    private int sys, dia;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_tensao);

        Util.fullscreen(this);


        timer = new Timer();
        TimerTask myTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tickCount++;
                    }
                });
            }
        };

        timer.schedule(myTask, 1000, 1000);

        self = SensorTensaoActivity.this;
        BLECenter.DEBUG_LOG_ON = true;
        BLECenter.DEBUG_LOG_LEVEL = 1;
        bleCenter = BLEContext.getBLECenter(getApplicationContext());

        initParams();
    }


    @SuppressLint("WrongViewCast")
    private void initParams() {
        titulo = findViewById(R.id.txt_title_bar);
        titulo.setText(getString(R.string.tensiometro));
        btnSair = findViewById(R.id.btnSair);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnSeguinte = findViewById(R.id.btnSeguinte);
        txt_data_hora = findViewById(R.id.txt_data_hora);
        valorSYS = findViewById(R.id.valorSYS);
        valorDIA = findViewById(R.id.valorDIA);
        txtEstado = findViewById(R.id.txtEstado);


        btnSeguinte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((bpm != null) && bpm.hasConnection()) {
                    bpm.disconnect();
                }

                Intent intent = new Intent(SensorTensaoActivity.this, SensorOxygen.class);
                startActivity(intent);
            }
        });


        btnSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), Menu_escolhas.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void startLEScan() {
        bleCenter.startBLEScan(DeviceFactory.Device.BLOOD_PRESSURE_MONITOR.getScanOption());


        new Thread(new Runnable() {
            Boolean check = true;

            public void run() {

                while (check) {

                    if (!bleCenter.getDevices().isEmpty()) {
                        ArrayList<GenericDevice> device = bleCenter.getDevices();
                        for (GenericDevice g : device) {

                            if (g instanceof BloodPressureMonitor) {
                                g.connect();
                                bpm = (BloodPressureMonitor) bleCenter.getDevice(g.getBleDevice().getAddress());
                                mac = g.getBleDevice().getAddress();
                                txtEstado.setText("Dispositivo conectado");
                                hideLoading();

                                Log.i("LOG_APP", mac);
                                if (ActivityCompat.checkSelfPermission(SensorTensaoActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                Log.i("LOG_APP", g.getBleDevice().getName());

                                stopLEScan();
                                check = false;
                            }
                        }
                    }
                }

                if (!check) {
                    Thread.interrupted();
                }
            }
        }).start();

    }

    private void stopLEScan() {
        bleCenter.stopBLEScan();
    }


    @Override
    protected void onResume() {
        super.onResume();

        timeOut = new TimeOut(this);
        txtEstado.setText("A encontrar dispositivo ...");
        txt_data_hora.setText(Util.currentDataTime());


        showLoading();
        startLEScan();
        BLECenter.bus().register(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        stopLEScan();

        if ((bpm != null) && bpm.hasConnection()) {
            bpm.disconnect();
        }

        timeOut.stopHandler();
        timer.cancel();

        BLECenter.bus().unregister(this);

    }

    private void showLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout relativeLayout = findViewById(R.id.tensio_activity_main_content);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                if (progressBar == null) {
                    progressBar = new ProgressBar(self, null, android.R.attr.progressBarStyleLarge);
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.VISIBLE);
                }
                relativeLayout.addView(progressBar, params);
            }
        });
    }

    private void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.tensio_activity_main_content);
                relativeLayout.removeView(progressBar);
            }
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onBPMStateEvent(BPMStateEvent event) {
        if (event.getMac().equalsIgnoreCase(mac)) {
            switch (event.getBpmState()) {
                case BPMStateStart:
                    updateCmdState("A medir ...");
                    break;
                case BPMStateOnButtonStart:
                    updateCmdState("Carregou no botão");
                    break;
                case BPMStateInProgress:
                    hideLoading();
                    updateCmdState("A realizar medição ...");
                    updateBPMState(event.getDataHashMap().get(BLEEvent.KEY_PRESSURE) + "");
                    break;
                case BPMStateOnButtonStop:
                    updateCmdState("Carregou no botão STOP");
                    break;
                case BPMStateFailure:
                    updateCmdState("Falha na medição");
                    break;
                case BPMStateOnButtonTurnOff:
                    updateCmdState("Carregou no botão");
                    break;
                case BPMStateAutoTurnOff:
                    updateCmdState("Dispositivo desligou-se");
                    hideLoading();
                    break;
                case BPMStateLowBatteryLevel:
                    updateCmdState("Baixo nível de bateria");
                    hideLoading();
                    break;
                case BPMStateSuccess:
                    updateCmdState("Sucesso na medição");
                    break;
                case BPMStateOnBTStopOrTurnOff:
                    updateCmdState(".btStopOrTurnOff");
                    break;
                case BPMStateOnBTSwitchModeMAM:
                    updateCmdState(".btSwitchModeMAM");
                    break;
                case BPMStateOnBTSwitchModeSingle:
                    updateCmdState(".btSwitchModeSingle");
                    break;
                case BPMStateDisplayInitialization:
                    updateCmdState(".displayInitialization");
                    break;
                case BPMStateOnButtonSwitchModeMAM:
                    updateCmdState(".btnSwitchModeMAM");
                    break;
                case BPMStateOnButtonSwitchModeSingle:
                    updateCmdState(".btnSwitchModeSingle");
                    break;
                default:
                    break;
            }
        }
    }


    @Subscribe
    @SuppressWarnings("unused")
    public void onBPMMeasurementEvent(BPMMeasurementEvent event) {
        if (event.getMac().equalsIgnoreCase(mac)) {
            updateResult(event.getBpmData());
        }
    }

    private void updateCmdState(final String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtEstado.setText(state);
            }
        });
    }

    private void updateBPMState(final String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                valorDIA.setText(state);
            }
        });
    }

    private void updateResult(final BPMData data) {
        runOnUiThread(new Runnable() {

            Socket socket;
            DataOutputStream dos;

            @Override
            public void run() {
                if (data != null) {
                    sys = data.getSystolic();
                    dia = data.getDiastolic();
                    valorSYS.setText(Integer.toString(sys));
                    valorDIA.setText(Integer.toString(dia));
                }


                try {
                    Thread.sleep(1000);
                    //Log.d("MYINT", "value2: "+sys);
                    String frase1= Float.toString(sys);
                    String frase2= Float.toString(dia);
                    String msg= "S2: "+frase1+"(SYS)"+" "+frase2+"(DIA)";
                    socket = new Socket("100.125.148.126", 12345);
                    dos = new DataOutputStream(socket.getOutputStream());

                    dos.write(msg.getBytes(StandardCharsets.UTF_8));
                    dos.close();
                    dos.flush();
                    socket.close();



                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    public void back_menu(View view) {
        Intent in = new Intent(SensorTensaoActivity.this, Menu_escolhas.class);
        startActivity(in);
    }
}