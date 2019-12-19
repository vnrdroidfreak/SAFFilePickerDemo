package com.virudhairaj.saf.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {

    Handler handler;
    private Runnable navRunnable=new Runnable() {
        @Override
        public void run() {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler = new Handler();
        handler.postDelayed(navRunnable, 1000);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navRunnable);
        super.onDestroy();
    }
}
