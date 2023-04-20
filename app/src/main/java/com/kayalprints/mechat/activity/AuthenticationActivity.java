package com.kayalprints.mechat.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kayalprints.mechat.R;
import com.kayalprints.mechat.classes.Operations;
import com.kayalprints.mechat.databinding.ActivityAuthenticationBinding;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AuthenticationActivity extends AppCompatActivity {

    ActivityAuthenticationBinding binding;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference databaseReference = database.getReference().child("UsersData");

    private String codeSent, createdTime;

    private boolean twiceBack = false;
    private Boolean haveData = false;

    @Override
    protected void onStart() {
        Log.i("ashis", "onStart");

        super.onStart();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
            finish();
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressbarAuth.setVisibility(View.INVISIBLE);
        binding.buttonGetCode.setBackgroundResource(R.drawable.inputbutton);

        setAnimations();

        binding.buttonGetCode.setOnClickListener(view -> {
            binding.buttonGetCode.setClickable(false);
            binding.progressbarAuth.setVisibility(View.VISIBLE);
            String phNo = Operations.extractPhoneNumber(binding.editTextPhoneSignIn.getText().toString().trim());
            if(!phNo.isEmpty())
                sendCode("+91"+phNo);
            else Toast.makeText(AuthenticationActivity.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
        });

        binding.buttonVerifyCode.setOnClickListener(view -> {
            binding.progressbarAuth.setVisibility(View.VISIBLE);
            signWithPhoneCode();
            binding.buttonGetCode.setClickable(true);
        });


    }

    private void sendCode(String ph) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(ph)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(AuthenticationActivity.this) // Must call
                .setCallbacks(callBacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks callBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(AuthenticationActivity.this, "Code can't sent", Toast.LENGTH_SHORT).show();
            binding.buttonGetCode.setClickable(true);
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent = s;
            Toast.makeText(AuthenticationActivity.this, "Code is sent", Toast.LENGTH_SHORT).show();
        }
    };

    private void signWithPhoneCode() {
        String otpEntered = binding.editTextPhCode.getText().toString().trim();
        if(!otpEntered.isEmpty()) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, otpEntered);

            auth.signInWithCredential(credential)
                    .addOnCompleteListener(AuthenticationActivity.this, task -> {

                        if(task.isSuccessful()) {

                            Intent i = createUserDB();
                            i.putExtra("createdDate",createdTime);
                            startActivity(i);
                            finish();

                        } else Toast.makeText(AuthenticationActivity.this, "Code incorrect", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(AuthenticationActivity.this, "OTP verification is failed", Toast.LENGTH_SHORT).show())
                    .addOnCanceledListener(() -> Toast.makeText(AuthenticationActivity.this, "OTP verification is canceled", Toast.LENGTH_SHORT).show());

        } else Toast.makeText(AuthenticationActivity.this, "Please enter otp", Toast.LENGTH_SHORT).show();
    }

    private Intent createUserDB() {
        FirebaseUser user = auth.getCurrentUser();
        Intent i = new Intent(AuthenticationActivity.this, ProfileDataSetOnceActivity.class);

        if(user != null) {
            DatabaseReference reference = databaseReference.child(Objects.requireNonNull(user.getPhoneNumber()));
            reference.child("haveData").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    haveData = (Boolean) snapshot.getValue();
                    Log.i("ashis", "in createDB-onSuccess have data =  "+haveData);

                    if(haveData == null || !haveData) setDefaultValue(reference); // If there is no haveData object in DB then haveData is not created
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    haveData = false;
                    Log.i("ashis", "in createDB-onFail have data =  "+haveData);

                    setDefaultValue(reference);
                }
            });
        }
        return i;
    }

    private void setDefaultValue(DatabaseReference reference) {
        reference.child("haveData").setValue(true)
                .addOnFailureListener(ei -> Toast.makeText(AuthenticationActivity.this, "User Database creation failed", Toast.LENGTH_SHORT).show());
        reference.child("name").setValue("null");
        reference.child("dp").setValue("null");
        createdTime = getNowDate();
        reference.child("DOJoining").setValue(createdTime);
    }

    private String getNowDate() {
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E, dd MMMM yyyy, HH:mm:ss");
        return time.format(formatter);
    }

    private void setAnimations() {
        binding.editTextPhoneSignIn.setTranslationX(-300);
        binding.buttonGetCode.setTranslationX(300);
        binding.editTextPhCode.setTranslationX(-300);
        binding.buttonVerifyCode.setTranslationX(300);

        binding.buttonGetCode.setAlpha(0);
        binding.buttonVerifyCode.setAlpha(0);
        binding.editTextPhoneSignIn.setAlpha(0);
        binding.editTextPhCode.setAlpha(0);

        long duration = 1000;

        binding.buttonGetCode.animate().translationX(0).alpha(1f).setDuration(duration).setStartDelay(500).start();
        binding.buttonVerifyCode.animate().translationX(0).alpha(1f).setDuration(duration).setStartDelay(500).start();
        binding.editTextPhoneSignIn.animate().translationX(0).alpha(1f).setDuration(duration).setStartDelay(500).start();
        binding.editTextPhCode.animate().translationX(0).alpha(1f).setDuration(duration).setStartDelay(500).start();
    }

    @Override
    public void onBackPressed() {
        if(twiceBack) super.onBackPressed();
        else {
            twiceBack = true;
            Toast.makeText(this, "Press back button again to exit.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onRestart() {
        Log.i("ashis", "onRestart");
        binding.progressbarAuth.setVisibility(View.INVISIBLE);
        super.onRestart();
    }
}