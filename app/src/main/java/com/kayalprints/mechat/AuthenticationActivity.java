package com.kayalprints.mechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AuthenticationActivity extends AppCompatActivity {

    EditText phoneNumber, otp;
    Button getCode, verify;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = database.getReference().child("UsersData");

    String codeSent;

    private boolean twiceBack = false;

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        phoneNumber = findViewById(R.id.editTextPhoneSignIn);
        otp = findViewById(R.id.editTextPhCode);
        getCode = findViewById(R.id.buttonGetCode);
        verify = findViewById(R.id.buttonVerifyCode);

        getCode.setOnClickListener(view -> {
            String phNo = phoneNumber.getText().toString().trim();
            if(!phNo.isEmpty())
                sendCode("+91"+phNo);
            else Toast.makeText(AuthenticationActivity.this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
        });

        verify.setOnClickListener(view -> signWithPhoneCode());


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
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent = s;
            Toast.makeText(AuthenticationActivity.this, "Code is sent", Toast.LENGTH_SHORT).show();
        }
    };

    private void signWithPhoneCode() {
        String otpEntered = otp.getText().toString().trim();
        if(!otpEntered.isEmpty()) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, otpEntered);

            auth.signInWithCredential(credential)
                    .addOnCompleteListener(AuthenticationActivity.this, task -> {

                        if(task.isSuccessful()) {

                            createUserDB();
                            startActivity(new Intent(AuthenticationActivity.this, ProfileActivity.class));
                            finish();

                        } else Toast.makeText(AuthenticationActivity.this, "Code incorrect", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(AuthenticationActivity.this, "OTP verification is failed", Toast.LENGTH_SHORT).show())
                    .addOnCanceledListener(() -> Toast.makeText(AuthenticationActivity.this, "OTP verification is canceled", Toast.LENGTH_SHORT).show());

        } else Toast.makeText(AuthenticationActivity.this, "Please enter otp", Toast.LENGTH_SHORT).show();
    }

    private void createUserDB() {
        FirebaseUser user = auth.getCurrentUser();
        if(user != null) {
            DatabaseReference reference = databaseReference.child(user.getUid());
            reference.child("name").setValue("null")
                    .addOnFailureListener(e -> Toast.makeText(AuthenticationActivity.this, "User Database creation failed", Toast.LENGTH_SHORT).show());
            reference.child("dp").setValue("null");
            reference.child("DOJoining").setValue(getNowDate());
        }
    }

    private String getNowDate() {
        Date date = new Date(System.currentTimeMillis());
        return date.toString();
    }

    @Override
    public void onBackPressed() {
        if(twiceBack) super.onBackPressed();
        else {
            twiceBack = true;
            Toast.makeText(this, "Press back button again to exit.", Toast.LENGTH_SHORT).show();
        }
    }


}

















