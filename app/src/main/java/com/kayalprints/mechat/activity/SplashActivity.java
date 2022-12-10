package com.kayalprints.mechat.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.kayalprints.mechat.R;

import java.lang.reflect.Method;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ImageView splashImage, retry;
    private ProgressBar progressBar;
    private ConstraintLayout layout;
    private TextView errorMassage;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashImage = findViewById(R.id.splashImage);
        layout = findViewById(R.id.splashLay);
        progressBar = findViewById(R.id.progressBarSplash);
        retry = findViewById(R.id.imageViewRetry);
        errorMassage = findViewById(R.id.textViewDataError);

        retry.setVisibility(View.INVISIBLE);
        errorMassage.setVisibility(View.INVISIBLE);

        checkDataConnection();

        retry.setOnClickListener(v -> {
            retry.setVisibility(View.INVISIBLE);
            errorMassage.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            new Handler().postDelayed(this::checkDataConnection, 2000);
        });




    }

    private void checkDataConnection() {
        boolean dataEnabled = false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class<?> cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            dataEnabled = (Boolean)method.invoke(cm);
        } catch (Exception e) {
            // Some problem accessible private API
            // TODO do whatever error handling you want here
        }

        if(dataEnabled) {
            splashImage.setImageResource(R.drawable.logo);

            splashImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splashimageanim));

            new Handler().postDelayed(() -> {
                finish();
                startActivity(new Intent(SplashActivity.this, AuthenticationActivity.class));
            }, 3000);
        } else {
            splashImage.setImageResource(R.drawable.ic_baseline_error);
            progressBar.setVisibility(View.INVISIBLE);
            errorMassage.setText(R.string.internet_error);
            retry.setVisibility(View.VISIBLE);
            errorMassage.setVisibility(View.VISIBLE);
        }

    }
}