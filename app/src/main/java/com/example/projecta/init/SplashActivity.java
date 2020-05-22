package com.example.projecta.init;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.example.projecta.R;

/**
 * Clase que se ejecuta siempre que se inicia o se vuelve a la aplicación, muestra
 * el logo de Ubishot hasta que cargue la siguiente actividad.
 */

public class SplashActivity extends AppCompatActivity {

    private final static String TAG = "SplashActivity";
    private final static int SPLASH_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Le indico a la ventana que debe mostrarlo en pantalla completa.
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        //Creo un nuevo manejador en el que despues del tiempo indicado ejecutará el Runnable que
        // iniciará la actividad para loguearse.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DURATION);

    }
}
