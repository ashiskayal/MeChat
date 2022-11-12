package com.kayalprints.mechat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    ImageView splashImage;

    ConstraintLayout layout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashImage = findViewById(R.id.splashImage);
        layout = findViewById(R.id.splashLay);

        splashImage.setImageResource(R.drawable.logo);

        splashImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.splashimageanim));

        new Handler().postDelayed(() -> {
            finish();
            startActivity(new Intent(SplashActivity.this, AuthenticationActivity.class));
        }, 3000);

    }
}