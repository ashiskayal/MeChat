package com.kayalprints.mechat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.kayalprints.mechat.R;
import com.kayalprints.mechat.databinding.ActivitySettingBinding;

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Setting");
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }
}