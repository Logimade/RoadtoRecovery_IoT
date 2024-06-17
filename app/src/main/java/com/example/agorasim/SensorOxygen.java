package com.example.agorasim;

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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import util.TimeOut;
import util.Util;
import visiomed.fr.bleframework.common.BLECenter;
import visiomed.fr.bleframework.common.BLEContext;
import visiomed.fr.bleframework.device.DeviceFactory;
import visiomed.fr.bleframework.device.GenericDevice;
import visiomed.fr.bleframework.device.Oximeter;
import visiomed.fr.bleframework.event.common.BLEDeviceStateEvent;
import visiomed.fr.bleframework.event.oximeter.OximeterEvent;


public class SensorOxygen extends AppCompatActivity {

    static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    int tickCount = 0;
    private String mac;
    private BLECenter bleCenter;
    private TimeOut timeOut;
    private Button btnSeguinte, btnSair, btnVoltar;
    private TextView titulo, txtSP, txt_data_hora, txtEstado, valorFC, valorSP ;
    private ProgressBar progressBar;
    private SensorOxygen self;
    private Oximeter oximeter;
    private int oxygenContent = -1;
    private int pulse = -1;
    private Timer timer;
    private int flagTemp = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_oxygen);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Util.fullscreen(this);

        btnSair = findViewById(R.id.btnSair);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnSeguinte = findViewById(R.id.btnSeguinte);

        btnSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SensorOxygen.this, Menu_escolhas.class);
                startActivity(intent);
                finish();
            }
        });
        btnSeguinte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((oximeter != null) && oximeter.hasConnection()) {
                    oximeter.disconnect();
                }

                Intent intent = new Intent(SensorOxygen.this, FinalActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SensorOxygen.this, SensorTensaoActivity.class);
                startActivity(intent);
                finish();
            }
        });

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

        self = SensorOxygen.this;
        BLECenter.DEBUG_LOG_ON = true;
        BLECenter.DEBUG_LOG_LEVEL = 1;
        bleCenter = BLEContext.getBLECenter(getApplicationContext());

        initParams();
    }

    private void initParams() {
        titulo = findViewById(R.id.txt_title_bar);
        titulo.setText(getString(R.string.oximetro));
        valorSP = findViewById(R.id.valorSP);
        txt_data_hora = findViewById(R.id.txt_data_hora);
        txtEstado = findViewById(R.id.txtEstado);

        txtSP = findViewById(R.id.txtSP);

        final String s = "SpO₂";
        txtSP.setText(s);

    }

    private void startLEScan() {
        bleCenter.startBLEScan(DeviceFactory.Device.OXIMETER.getScanOption());


        new Thread(new Runnable() {
            Boolean check = true;

            public void run() {

                while (check) {

                    if (!bleCenter.getDevices().isEmpty()) {
                        ArrayList<GenericDevice> device = bleCenter.getDevices();
                        for (GenericDevice g : device) {

                            if (g instanceof Oximeter) {
                                g.connect();
                                oximeter = (Oximeter) bleCenter.getDevice(g.getBleDevice().getAddress());
                                mac = g.getBleDevice().getAddress();

                                Log.i("LOG_APP", mac);
                                if (ActivityCompat.checkSelfPermission(SensorOxygen.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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
        //btnSeguinte.setVisibility(View.INVISIBLE);
        showLoading();
        startLEScan();

        BLECenter.bus().register(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        stopLEScan();

        if ((oximeter != null) && oximeter.hasConnection()) {
            oximeter.disconnect();
        }

        timeOut.stopHandler();
        timer.cancel();
        //    if (TempDataStore.interfaceList.get(17).getInterfaceTime() == 0) {
        //        TempDataStore.interfaceList.get(17).setInterfaceTime(tickCount);
        //        Log.i("LOG_APP", Integer.toString(tickCount));
        //    }

        BLECenter.bus().unregister(this);

    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onOximeterEvent(final OximeterEvent event) {

        self.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                oxygenContent = event.getOximeterData().getOxygenContent();
                pulse = event.getOximeterData().getPulse();

                if (oxygenContent > 0 && pulse > 0) {
                    valorFC.setText(Integer.toString(pulse));
                    valorSP.setText(Integer.toString(oxygenContent));

                    Util.fullscreen(SensorOxygen.this);
                    txtEstado.setText(R.string.sucesso_medicao);
                    // btnSeguinte.setVisibility(View.VISIBLE);
                    // btnSeguinte.setAnimation(AnimationUtils.loadAnimation(SensorOxyActivity.this,R.anim.shake_button));
                }

            }
        });
    }

    private void showLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout relativeLayout = findViewById(R.id.oximeter_activity_main_content);
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
                RelativeLayout relativeLayout = findViewById(R.id.oximeter_activity_main_content);
                relativeLayout.removeView(progressBar);
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
                        txtEstado.setText("A realizar medição ...");
                        hideLoading();
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_DISCONNECTING) {
                        txtEstado.setText(R.string.state_disconnecting);
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_DISCONNECTED) {
                        // txtEstado.setText(R.string.action_connect);
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_GATT_FAILED) {
                        txtEstado.setText("Dispositivo desligado");

                        if ((pulse < 0 || oxygenContent < 0)) {
                            showLoading();
                            startLEScan();
                            txtEstado.setText("A encontrar dispositivo ...");

                        } else {
                            hideLoading();
                        }
                    }
                }
            }
        });
    }

}