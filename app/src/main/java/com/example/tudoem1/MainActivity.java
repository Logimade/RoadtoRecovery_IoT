package com.example.tudoem1;

import androidx.appcompat.app.AppCompatActivity;

import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.tudoem1.gpsUtils.GPService;

import util.Util;

public class MainActivity extends AppCompatActivity {

    private Button btnSettings;
    private Button btnStart;

    private Button btnNetwork;
    private static final int REQUEST_CODE_BLUETOOTH_SCAN = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Util.fullscreen(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_CODE_BLUETOOTH_SCAN);
            }
        }


        btnStart = findViewById(R.id.btnStart);
        btnSettings = findViewById(R.id.btnSettings);
        btnNetwork = findViewById(R.id.btnNetwork);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MedicoesActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkGpsStatus()) {
                    Intent i = new Intent(getApplicationContext(), GPService.class);
                    startService(i);
                }

                Intent intent = new Intent(MainActivity.this, NetMonsterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_BLUETOOTH_SCAN) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão foi concedida, continue com a operação de scan
            } else {
                // Permissão foi negada, trate o caso adequadamente
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TempDataStore temp = new TempDataStore();

    }

    private boolean checkGpsStatus() {
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}