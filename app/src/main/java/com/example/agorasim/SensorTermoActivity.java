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

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import util.TimeOut;
import util.Util;
import visiomed.fr.bleframework.common.BLECenter;
import visiomed.fr.bleframework.common.BLEContext;
import visiomed.fr.bleframework.data.thermometer.ThermometerData;
import visiomed.fr.bleframework.device.DeviceFactory;
import visiomed.fr.bleframework.device.GenericDevice;
import visiomed.fr.bleframework.device.Thermometer;
import visiomed.fr.bleframework.event.common.BLEDeviceStateEvent;
import visiomed.fr.bleframework.event.thermometer.ThermometerEvent;

public class SensorTermoActivity extends AppCompatActivity {

    int tickCount = 0;
    private String mac;
    private TextView txt_thermo, titulo, txtEstado, txt_data_hora;
    private BLECenter bleCenter;
    private Thermometer thermometer;
    private TimeOut timeOut;
    private SensorTermoActivity self;
    private Button btnSeguinte, btnSair, btnVoltar;
    private ProgressBar progressBar;
    private static Float temperatura = 0.0F;
    private Timer timer;
    private int flagTemp = 0;

    //MyThread myThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensortermoactivity);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //myThread = new MyThread("");
        //new Thread(myThread).start();


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


        self = SensorTermoActivity.this;
        BLECenter.DEBUG_LOG_ON = true;
        BLECenter.DEBUG_LOG_LEVEL = 1;
        bleCenter = BLEContext.getBLECenter(getApplicationContext());

        initParams();
    }

    private void initParams() {
        titulo = findViewById(R.id.txt_title_bar);
        titulo.setText(getString(R.string.termometro));
        titulo.setText(getString(R.string.termometro));
        btnSair = findViewById(R.id.btnSair);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnSeguinte = findViewById(R.id.btnSeguinte);
        txt_thermo = findViewById(R.id.txt_thermo);
        txtEstado = findViewById(R.id.txtEstado);
        txt_data_hora = findViewById(R.id.txt_data_hora);


        btnSeguinte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((thermometer != null) && thermometer.hasConnection()) {
                    thermometer.disconnect();
                }

                Intent intent = new Intent(SensorTermoActivity.this, SensorTensaoActivity.class);
                startActivity(intent);

            }
        });


        btnSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
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
        bleCenter.startBLEScan(DeviceFactory.Device.THERMOMETER.getScanOption());


        new Thread(new Runnable() {
            Boolean check = true;

            public void run() {

                while (check) {

                    if (!bleCenter.getDevices().isEmpty()) {
                        ArrayList<GenericDevice> device = bleCenter.getDevices();
                        for (GenericDevice g : device) {

                            if (g instanceof Thermometer) {
                                g.connect();
                                thermometer = (Thermometer) bleCenter.getDevice(g.getBleDevice().getAddress());
                                mac = g.getBleDevice().getAddress();

                                Log.i("LOG_APP", mac);
                                if (ActivityCompat.checkSelfPermission(SensorTermoActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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
        showLoading();
        txt_data_hora.setText(Util.currentDataTime());

        startLEScan();
        BLECenter.bus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopLEScan();
        if ((thermometer != null) && thermometer.hasConnection()) {
            thermometer.disconnect();
        }

        timer.cancel();
        timeOut.stopHandler();

        BLECenter.bus().unregister(this);

    }

    public void updateResult(final ThermometerData data) {
        runOnUiThread(new Runnable() {
            Socket socket;
            DataOutputStream dos;

            @Override
            public void run() {
                if (data != null) {

                    temperatura = ((float) data.getTemperature()) / 10.0F;
                    Log.d("MYINT", "value1: " + temperatura);


                    if (temperatura < 32 || temperatura > 43) {
                        flagTemp = 1;
                        temperatura = 1F;
                        txtEstado.setText("A encontrar dispositivo ...");
                        startLEScan();

                    } else {
                        txt_thermo.setText(Float.toString(temperatura));

                    }
                }
                //UNCOMMENT TO CONNECT TO SOCKET_SERVER
                /*
                try {
                    Thread.sleep(1000);
                    Log.d("MYINT", "value2: "+temperatura);
                    String frase= Float.toString(temperatura);
                    String msg= "S: temp:"+frase;
                    //String str = String.valueOf(temperatura);
                    socket = new Socket("100.125.148.126", 12345);
                    dos = new DataOutputStream(socket.getOutputStream());

                    dos.write(msg.getBytes(StandardCharsets.UTF_8));
                    dos.close();
                    dos.flush();
                    socket.close();



                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
            } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }*/
            }
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onThermometerEvent(final ThermometerEvent event) {
        if (event.getMac().equalsIgnoreCase(mac)) {
            updateResult(event.getThermometerData());
        }
    }

    private void showLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RelativeLayout relativeLayout = findViewById(R.id.thermometer_activity_main_content);
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
                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.thermometer_activity_main_content);
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
                        txtEstado.setText("Medição realizada");
                        hideLoading();
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_DISCONNECTING) {
                        txtEstado.setText(R.string.state_disconnecting);
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_GATT_FAILED) {
                        txtEstado.setText("Dispositivo desligado");

                        if (temperatura == null) {
                            if (flagTemp == 0) {
                                txtEstado.setText("Medição realizada");
                            }
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