package com.example.tudoem1;

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
import visiomed.fr.bleframework.data.ecg.ECGRealTimeData;
import visiomed.fr.bleframework.data.thermometer.ThermometerData;
import visiomed.fr.bleframework.device.BloodPressureMonitor;
import visiomed.fr.bleframework.device.DeviceFactory;
import visiomed.fr.bleframework.device.GenericDevice;
import visiomed.fr.bleframework.device.Oximeter;
import visiomed.fr.bleframework.device.Thermometer;
import visiomed.fr.bleframework.device.ecg.ECG;
import visiomed.fr.bleframework.event.bpm.BPMMeasurementEvent;
import visiomed.fr.bleframework.event.bpm.BPMStateEvent;
import visiomed.fr.bleframework.event.common.BLEDeviceStateEvent;
import visiomed.fr.bleframework.event.common.BLEEvent;
import visiomed.fr.bleframework.event.oximeter.OximeterEvent;
import visiomed.fr.bleframework.event.thermometer.ThermometerEvent;


public class MedicoesActivity extends AppCompatActivity {

    int tickCount = 0;
    private String mac;
    private TextView txt_thermo;
    private TextView txtEstado_Thermo;
    private TextView txt_data_hora;
    private TextView valorSYS;
    private TextView valorDIA;
    private TextView txtEstado_Tensio;
    private TextView valorHR;
    private TextView txtEstado_Ecg;
    private TextView txtEstado_Oxy;
    private TextView valorSP;
    private TextView valorFC;
    private BLECenter bleCenter;
    private Thermometer thermometer;
    private BloodPressureMonitor bpm;
    private Oximeter oximeter;
    private ECG ecg;
    private TimeOut timeOut;
    private MedicoesActivity self;
    private ProgressBar progressBar;
    private static Float temperatura = 0.0F;
    private int oxygenContent = -1;
    private int pulse = -1;
    private int sys, dia;
    private int HR;
    private Timer timer;
    private int flagTemp = 0;
    public String ip1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicoes);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        Button btnVoltar = findViewById(R.id.btnVoltar);

        ip1 = getIntent().getStringExtra("Ip_text");


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

        btnVoltar.setOnClickListener(view -> {
            Intent intent = new Intent(MedicoesActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });


        self = MedicoesActivity.this;
        BLECenter.DEBUG_LOG_ON = true;
        BLECenter.DEBUG_LOG_LEVEL = 1;
        bleCenter = BLEContext.getBLECenter(getApplicationContext());
    }


    private void startLEScan_Thermo() {
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
                                if (ActivityCompat.checkSelfPermission(MedicoesActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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

                                stopLEScan_Thermo();
                                check = false;
                            }
                        }
                    }
                }

                Thread.interrupted();
            }
        }).start();

    }

    private void stopLEScan_Thermo() {
        bleCenter.stopBLEScan();
    }

    @Override
    protected void onResume() {
        super.onResume();

        timeOut = new TimeOut(this);
        txtEstado_Thermo.setText("Searching for sensor");
        showLoading_Thermo();

        txtEstado_Tensio.setText("Searching for sensor");
        showLoading_Tensio();

        txtEstado_Ecg.setText("Searching for sensor");
        showLoading_Ecg();

        txtEstado_Oxy.setText("Searching for sensor");
        showLoading_Oxy();

        txt_data_hora.setText(Util.currentDataTime());

        startLEScan_Thermo();
        startLEScan_Tensio();
        startLEScan_Ecg();
        startLEScan_Oxy();
        BLECenter.bus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopLEScan_Thermo();
        if ((thermometer != null) && thermometer.hasConnection()) {
            thermometer.disconnect();
        }
        stopLEScan_Tensio();
        if ((bpm != null) && bpm.hasConnection()) {
            bpm.disconnect();
        }

        stopLEScan_Ecg();

        if ((ecg != null) && ecg.hasConnection()) {
            ecg.disconnect();
        }

        stopLEScan_Oxy();

        if ((oximeter != null) && oximeter.hasConnection()) {
            oximeter.disconnect();
        }

        timer.cancel();
        timeOut.stopHandler();

        BLECenter.bus().unregister(this);

    }

    public void updateResult_Thermo(final ThermometerData data) {
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
                        txtEstado_Thermo.setText("Searching for sensor");
                        startLEScan_Thermo();

                    } else {
                        txt_thermo.setText(Float.toString(temperatura));

                    }
                }
                //UNCOMMENT TO CONNECT TO SOCKET_SERVER


                try {
                    Thread.sleep(1000);
                    //Log.d("MYINT", "value2: "+temperatura);
                    String frase = Float.toString(temperatura);
                    String msg = "S1: " + frase;
                    //String str = String.valueOf(temperatura);
                    socket = new Socket(ip1, 12345);
                    dos = new DataOutputStream(socket.getOutputStream());

                    dos.write(msg.getBytes(StandardCharsets.UTF_8));
                    dos.close();
                    dos.flush();
                    socket.close();


                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onThermometerEvent(final ThermometerEvent event) {
        if (event.getMac().equalsIgnoreCase(mac)) {
            updateResult_Thermo(event.getThermometerData());
        }
    }

    private void showLoading_Thermo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                RelativeLayout relativeLayout = findViewById(R.id.thermometer_activity_main_content);
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                params.addRule(RelativeLayout.CENTER_IN_PARENT);
//
//                // Verifica se progressBar já tem um pai e remove se necessário
//                if (progressBar != null && progressBar.getParent() != null) {
//                    ((ViewGroup) progressBar.getParent()).removeView(progressBar);
//                }
//
//                if (progressBar == null) {
//                    progressBar = new ProgressBar(self, null, android.R.attr.progressBarStyleLarge);
//                    progressBar.setIndeterminate(false);
//                    progressBar.setVisibility(View.VISIBLE);
//                }
//
//                relativeLayout.addView(progressBar, params);
            }
        });
    }

    private void hideLoading_Thermo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.thermometer_activity_main_content);
//                relativeLayout.removeView(progressBar);
            }
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onBLEDeviceConnectionStateEvent_Thermo(final BLEDeviceStateEvent event) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (event.getMac().equalsIgnoreCase(mac)) {
                    if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_CONNECTING) {
                        txtEstado_Thermo.setText(R.string.state_connecting);
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_CONNECTED) {
                        txtEstado_Thermo.setText("Medição realizada");
                        hideLoading_Thermo();
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_DISCONNECTING) {
                        txtEstado_Thermo.setText(R.string.state_disconnecting);
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_GATT_FAILED) {
                        txtEstado_Thermo.setText("Dispositivo desligado");

                        if (temperatura == null) {
                            if (flagTemp == 0) {
                                txtEstado_Thermo.setText("Medição realizada");
                            }
                            startLEScan_Thermo();
                            txtEstado_Thermo.setText("Searching for sensor");


                        } else {
                            hideLoading_Thermo();
                        }
                    }
                }
            }
        });
    }


    private void startLEScan_Tensio() {
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
                                txtEstado_Tensio.setText("Dispositivo conectado");
                                hideLoading_Tensio();

                                Log.i("LOG_APP", mac);
                                if (ActivityCompat.checkSelfPermission(MedicoesActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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

                                stopLEScan_Tensio();
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

    private void stopLEScan_Tensio() {
        bleCenter.stopBLEScan();
    }


    private void showLoading_Tensio() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                RelativeLayout relativeLayout = findViewById(R.id.tensio_activity_main_content);
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                params.addRule(RelativeLayout.CENTER_IN_PARENT);
//
//                // Verifica se progressBar já tem um pai e remove se necessário
//                if (progressBar != null && progressBar.getParent() != null) {
//                    ((ViewGroup) progressBar.getParent()).removeView(progressBar);
//                }
//
//                if (progressBar == null) {
//                    progressBar = new ProgressBar(self, null, android.R.attr.progressBarStyleLarge);
//                    progressBar.setIndeterminate(false);
//                    progressBar.setVisibility(View.VISIBLE);
//                }
//
//                relativeLayout.addView(progressBar, params);
            }
        });
    }


    private void hideLoading_Tensio() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.tensio_activity_main_content);
//                relativeLayout.removeView(progressBar);
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
                    hideLoading_Tensio();
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
                    hideLoading_Tensio();
                    break;
                case BPMStateLowBatteryLevel:
                    updateCmdState("Baixo nível de bateria");
                    hideLoading_Tensio();
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
                txtEstado_Tensio.setText(state);
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
                    String frase1 = Float.toString(sys);
                    String frase2 = Float.toString(dia);
                    String msg = "S2: " + frase1 + "(SYS)" + " " + frase2 + "(DIA)";
                    socket = new Socket(ip1, 12345);
                    dos = new DataOutputStream(socket.getOutputStream());

                    dos.write(msg.getBytes(StandardCharsets.UTF_8));
                    dos.close();
                    dos.flush();
                    socket.close();


                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void startLEScan_Ecg() {
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
                                hideLoading_Ecg();

                                Log.i("LOG_APP", mac);
                                if (ActivityCompat.checkSelfPermission(MedicoesActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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

                                stopLEScan_Ecg();
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

    private void stopLEScan_Ecg() {
        bleCenter.stopBLEScan();
    }

    private void showLoading_Ecg() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                RelativeLayout relativeLayout = findViewById(R.id.ecg_activity_main_content);
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                params.addRule(RelativeLayout.CENTER_IN_PARENT);
//
//                // Verifica se progressBar já tem um pai e remove se necessário
//                if (progressBar != null && progressBar.getParent() != null) {
//                    ((ViewGroup) progressBar.getParent()).removeView(progressBar);
//                }
//
//                if (progressBar == null) {
//                    progressBar = new ProgressBar(self, null, android.R.attr.progressBarStyleLarge);
//                    progressBar.setIndeterminate(false);
//                    progressBar.setVisibility(View.VISIBLE);
//                }
//
//                relativeLayout.addView(progressBar, params);
            }
        });
    }

    private void hideLoading_Ecg() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.ecg_activity_main_content);
//                relativeLayout.removeView(progressBar);
            }
        });
    }

    private void updateResult_Ecg(final ECGRealTimeData data) {
        runOnUiThread(new Runnable() {

            Socket socket;
            DataOutputStream dos;

            @Override
            public void run() {
                if (data != null) {
                    HR = data.getHeartRate();
                    valorHR.setText(Integer.toString(HR));
                }

                try {
                    Thread.sleep(1000);
                    //Log.d("MYINT", "value2: "+temperatura);
                    String frase = Float.toString(HR);
                    String msg = "S3: " + frase;
                    //String str = String.valueOf(temperatura);
                    socket = new Socket(ip1, 12345);
                    dos = new DataOutputStream(socket.getOutputStream());

                    dos.write(msg.getBytes(StandardCharsets.UTF_8));
                    dos.close();
                    dos.flush();
                    socket.close();


                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onBLEDeviceConnectionStateEvent_Ecg(final BLEDeviceStateEvent event) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (event.getMac().equalsIgnoreCase(mac)) {
                    if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_CONNECTING) {
                        txtEstado_Ecg.setText(R.string.state_connecting);
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_CONNECTED) {
                        txtEstado_Ecg.setText("Medição realizada");
                        hideLoading_Ecg();
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_DISCONNECTING) {
                        txtEstado_Ecg.setText(R.string.state_disconnecting);
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_GATT_FAILED) {
                        txtEstado_Ecg.setText("Dispositivo desligado");
                    }
                }
            }
        });
    }

    private void startLEScan_Oxy() {
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
                                if (ActivityCompat.checkSelfPermission(MedicoesActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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

                                stopLEScan_Oxy();
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

    private void stopLEScan_Oxy() {
        bleCenter.stopBLEScan();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onOximeterEvent(final OximeterEvent event) {

        self.runOnUiThread(new Runnable() {

            Socket socket;
            DataOutputStream dos;

            @Override
            public void run() {
                oxygenContent = event.getOximeterData().getOxygenContent();
                pulse = event.getOximeterData().getPulse();

                if (oxygenContent > 0 && pulse > 0) {
                    valorFC.setText(Integer.toString(pulse));
                    valorSP.setText(Integer.toString(oxygenContent));

                    txtEstado_Oxy.setText(R.string.sucesso_medicao);

                    try {
                        Thread.sleep(1000);
                        //Log.d("MYINT", "value2: "+sys);
                        String frase1 = Float.toString(pulse);
                        String frase2 = Float.toString(oxygenContent);
                        String msg = "S4: " + frase1 + frase2;
                        socket = new Socket(ip1, 12345);
                        dos = new DataOutputStream(socket.getOutputStream());

                        dos.write(msg.getBytes(StandardCharsets.UTF_8));
                        dos.close();
                        dos.flush();
                        socket.close();


                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        });
    }

    private void showLoading_Oxy() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                RelativeLayout relativeLayout = findViewById(R.id.oximeter_activity_main_content);
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                params.addRule(RelativeLayout.CENTER_IN_PARENT);
//
//                // Verifica se progressBar já tem um pai e remove se necessário
//                if (progressBar != null && progressBar.getParent() != null) {
//                    ((ViewGroup) progressBar.getParent()).removeView(progressBar);
//                }
//
//                if (progressBar == null) {
//                    progressBar = new ProgressBar(self, null, android.R.attr.progressBarStyleLarge);
//                    progressBar.setIndeterminate(false);
//                    progressBar.setVisibility(View.VISIBLE);
//                }
//                relativeLayout.addView(progressBar, params);
            }
        });
    }

    private void hideLoading_Oxy() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                RelativeLayout relativeLayout = findViewById(R.id.oximeter_activity_main_content);
//                relativeLayout.removeView(progressBar);
            }
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onBLEDeviceConnectionStateEvent_Oxy(final BLEDeviceStateEvent event) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (event.getMac().equalsIgnoreCase(mac)) {
                    if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_CONNECTING) {
                        txtEstado_Oxy.setText(R.string.state_connecting);
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_CONNECTED) {
                        txtEstado_Oxy.setText("A realizar medição ...");
                        hideLoading_Oxy();
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_DISCONNECTING) {
                        txtEstado_Oxy.setText(R.string.state_disconnecting);
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_DISCONNECTED) {
                        // txtEstado.setText(R.string.action_connect);
                    } else if (event.getConnectionState() == BLEDeviceStateEvent.CONNECTION_STATE_GATT_FAILED) {
                        txtEstado_Oxy.setText("Dispositivo desligado");

                        if ((pulse < 0 || oxygenContent < 0)) {
                            showLoading_Oxy();
                            startLEScan_Oxy();
                            txtEstado_Oxy.setText("Searching for sensor");

                        } else {
                            hideLoading_Oxy();
                        }
                    }
                }
            }
        });
    }
}