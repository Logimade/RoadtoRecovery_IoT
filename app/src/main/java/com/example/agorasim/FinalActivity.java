package com.example.agorasim;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import util.Util;

public class FinalActivity extends AppCompatActivity {

    //private InserirDadosController inserirDadosController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);
        Util.fullscreen(this);

        //    TempDataStore.listValores();
        //    inserirDadosController = new InserirDadosController(getApplicationContext());
        //    inserirDadosController.inserirDadosBD();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent;

                intent = new Intent(FinalActivity.this,
                        MainActivity.class);


                startActivity(intent);
                finish();
                return;

            }
        }, Util.TIME_RELOAD);

    }

}