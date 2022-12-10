package com.kayalprints.mechat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.kayalprints.mechat.R;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Setting");
        setContentView(R.layout.activity_setting);
    }
}