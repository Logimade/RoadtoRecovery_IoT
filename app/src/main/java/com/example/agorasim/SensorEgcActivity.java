package com.example.agorasim;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
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
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import util.TimeOut;
import util.Util;
import visiomed.fr.bleframework.common.BLECenter;
import visiomed.fr.bleframework.common.BLEContext;
import visiomed.fr.bleframework.data.ecg.ECGRealTimeData;
import visiomed.fr.bleframework.device.DeviceFactory;
import visiomed.fr.bleframework.device.GenericDevice;
import visiomed.fr.bleframework.device.ecg.ECG;
import visiomed.fr.bleframework.event.common.BLEDeviceStateEvent;

public class SensorEgcActivity extends AppCompatActivity {

    int tickCount = 0;
    private String mac;
    private BLECenter bleCenter;
    private ECG ecg;
    private TimeOut timeOut;
    private TextView titulo, txt_data_hora, txt_egc, valorHR, txtEstado;
    private Button btnSair, btnVoltar;
    private SensorEgcActivity self;
    private ProgressBar progressBar;
    private Button btnSeguinte;
    private Timer timer;

    private int HR;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_egc);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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

        self = SensorEgcActivity.this;
        BLECenter.DEBUG_LOG_ON = true;
        BLECenter.DEBUG_LOG_LEVEL = 1;
        bleCenter = BLEContext.getBLECenter(getApplicationContext());

        initParams();
    }

    private void initParams() {
        titulo = findViewById(R.id.txt_title_bar);
        titulo.setText(getString(R.string.egc));
        btnSair = findViewById(R.id.btnSair);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnSeguinte = findViewById(R.id.btnSeguinte);
        txt_egc = findViewById(R.id.txt_egc);
        txt_data_hora = findViewById(R.id.txt_data_hora);
        txtEstado = findViewById(R.id.txtEstado);
        valorHR = findViewById(R.id.valorHR);

        btnSeguinte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((ecg != null) && ecg.hasConnection()) {
                    ecg.disconnect();
                }

                Intent intent = new Intent(SensorEgcActivity.this, SensorTensaoActivity.class);
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
        bleCenter.startBLEScan(DeviceFactory.Device.ECG.getScanOption());


        new Thread(new Runnable() {
            Boolean check = true;

            public void run() {

                while (check) {

                    if (!bleCenter.getDevices().isEmpty()) {
                        ArrayList<GenericDevice> device = bleCenter.getDevices();
                        for (GenericDevice g : device) {

                            if (g instanceof ECG) {
                                g.connect();
                                ecg = (ECG) bleCenter.getDevice(g.getBleDevice().getAddress());
                                mac = g.getBleDevice().getAddress();
                                hideLoading();

                                Log.i("LOG_APP", mac);
                                if (ActivityCompat.checkSelfPermission(SensorEgcActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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

    protected void onResume() {
        super.onResume();

        timeOut = new TimeOut(this);
        txt_data_hora.setText(Util.currentDataTime());
        txtEstado.setText("A encontrar dispositivo ...");


        showLoading();
        startLEScan();
        BLECenter.bus().register(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        stopLEScan();

        if ((ecg != null) && ecg.hasConnection()) {
            ecg.disconnect();
        }

        timeOut.stopHandler();
        timer.cancel();

        BLECenter.bus().unregister(this);

    }

    private void showLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout relativeLayout = findViewById(R.id.ecg_activity_main_content);
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
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.ecg_activity_main_content);
                relativeLayout.removeView(progressBar);
            }
        });
    }

    private void updateResult(final ECGRealTimeData data) {
        runOnUiThread(new Runnable() {

            Socket socket;
            DataOutputStream dos;

            @Override
            public void run() {
                if (data != null) {
                    HR = data.getHeartRate();
                    valorHR.setText(Integer.toString(HR));
                }

                /*
                try {
                    Thread.sleep(1000);
                    //Log.d("MYINT", "value2: "+sys);
                    String frase1= Float.toString(sys);
                    String frase2= Float.toString(sys);
                    String msg= "S: tens:"+frase1+frase2;
                    socket = new Socket("100.125.148.126", 12345);
                    dos = new DataOutputStream(socket.getOutputStream());

                    dos.write(msg.getBytes(StandardCharsets.UTF_8));
                    dos.close();
                    dos.flush();
                    socket.close();



                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }*/
            }
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onBLEDeviceConnectionStateEvent(final BLEDeviceStateEvent event) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (event.getMac().equalsIgnoreCase(mac)) {
                    if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_CONNECTING) {
                        txtEstado.setText(R.string.state_connecting);
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_CONNECTED) {
                        txtEstado.setText("Medição realizada");
                        hideLoading();
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_DISCONNECTING) {
                        txtEstado.setText(R.string.state_disconnecting);
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_GATT_FAILED) {
                        txtEstado.setText("Dispositivo desligado");
                    }
                }
            }
        });
    }
}