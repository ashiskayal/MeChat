package com.kayalprints.mechat.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.kayalprints.mechat.R;
import com.kayalprints.mechat.databinding.ActivitySplashBinding;

import java.lang.reflect.Method;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imageViewRetry.setVisibility(View.INVISIBLE);
        binding.textViewDataError.setVisibility(View.INVISIBLE);

        checkDataConnection();

        binding.imageViewRetry.setOnClickListener(v -> {
            binding.imageViewRetry.setVisibility(View.INVISIBLE);
            binding.textViewDataError.setVisibility(View.INVISIBLE);
            binding.progressBarSplash.setVisibility(View.VISIBLE);
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
            binding.splashImage.setImageResource(R.drawable.logo);

            binding.splashImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splashimageanim));

            new Handler().postDelayed(() -> {
                finish();
                startActivity(new Intent(SplashActivity.this, AuthenticationActivity.class));
            }, 3000);
        } else {
            binding.splashImage.setImageResource(R.drawable.ic_baseline_error);
            binding.progressBarSplash.setVisibility(View.INVISIBLE);
            binding.textViewDataError.setText(R.string.internet_error);
            binding.imageViewRetry.setVisibility(View.VISIBLE);
            binding.textViewDataError.setVisibility(View.VISIBLE);
        }

    }
}