    package com.example.agorasim;

    import android.content.Intent;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.Button;

    import androidx.appcompat.app.AppCompatActivity;

    import util.Util;

    public class Menu_escolhas extends AppCompatActivity {

        private Button back_menu;

        private Button btnSettings;

        private Button bt_oxy;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_menu_escolhas);

            Util.fullscreen(this);

            back_menu = findViewById(R.id.bt_back);
            btnSettings=findViewById(R.id.btnSettings);
            bt_oxy=findViewById(R.id.bt_oxy);
            back_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Menu_escolhas.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });


            btnSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Menu_escolhas.this, Settings.class);
                    startActivity(intent);
                    finish();
                }
            });

            bt_oxy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Menu_escolhas.this, SensorOxygen.class);
                    startActivity(intent);
                    finish();
                }
            });


        }

        public void back_menu(View view) {
            Intent in = new Intent(Menu_escolhas.this, MainActivity.class);
            startActivity(in);
        }
        public void go_to_temp(View view) {
            Intent in = new Intent(Menu_escolhas.this, SensorTermoActivity.class);
            startActivity(in);
        }
        public void go_to_EGC(View view) {
            Intent in = new Intent(Menu_escolhas.this, SensorEgcActivity.class);
            startActivity(in);
        }
        public void go_to_tensao(View view) {
            Intent in = new Intent(Menu_escolhas.this, SensorTensaoActivity.class);
            startActivity(in);
        }
    }