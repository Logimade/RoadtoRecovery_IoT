package com.example.tudoem1;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import util.Util;

public class SettingsActivity extends AppCompatActivity {

    private Button btnBackSettings;
    private Button btnSetIp;
    private EditText ip_text_input;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Util.fullscreen(this);

        btnBackSettings = findViewById(R.id.btnBackSettings);
        btnSetIp = findViewById(R.id.btnSetIp);
        ip_text_input= findViewById(R.id.ip_text_input);

        btnBackSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnSetIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip= ip_text_input.getText().toString();
                Intent intent = new Intent(SettingsActivity.this, MedicoesActivity.class);
                intent.putExtra("Ip_text", ip);
                startActivity(intent);
                finish();
            }
        });

    }
}