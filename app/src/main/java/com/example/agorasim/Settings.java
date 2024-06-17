package com.example.agorasim;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import util.Util;

public class Settings extends AppCompatActivity {

    private Button bt_back;

    private Button btnSetIp;

    private EditText ip_text_input;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Util.fullscreen(this);

        bt_back = findViewById(R.id.bt_back);
        btnSetIp = findViewById(R.id.btnSetIp);
        ip_text_input= findViewById(R.id.ip_text_input);

        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this, Menu_escolhas.class);
                startActivity(intent);
                finish();
            }
        });

        btnSetIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip= ip_text_input.getText().toString();
                Intent intent = new Intent(Settings.this, SensorTermoActivity.class);
                intent.putExtra("Ip_text", ip);
                startActivity(intent);
                finish();
            }
        });

    }
}