package com.kayalprints.mechat.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.kayalprints.mechat.R;
import com.kayalprints.mechat.classes.MeChatDatabase;
import com.kayalprints.mechat.classes.Operations;
import com.kayalprints.mechat.databinding.ActivityProfileBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseUser user;

    private ActivityProfileBinding binding;

    private ArrayList<String> userData;
    private Bitmap dp;

    private boolean editOn;
    private Boolean haveData;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        setContentView(binding.getRoot());

        DatabaseReference df = MeChatDatabase.getDatabaseReference();
        if(df != null) databaseReference = df.child("UsersData");
        storageReference = MeChatDatabase.getStorageReference(); // Can be null
        user = MeChatDatabase.getCurrentUser(); // Can be null

        userData = new ArrayList<>(3);

        getData();

        binding.profileCircleImageNewChat.setOnClickListener(v -> profileImageClicked());

        binding.imageViewEditname.setOnClickListener(v -> {
            editOn = !editOn;
            String userName = binding.editTextProfileName.getText().toString().trim();
            Operations.nameEdition(userName, binding.editTextProfileName, binding.imageViewEditname, editOn, ProfileActivity.this);
        });

        binding.signoutlay.setOnClickListener(v -> {
            MeChatDatabase.getAuth().signOut(); // Null check
            setResult(RESULT_OK, new Intent());
            finish();
        });

    }


    private void getData() {

        try { // If the database references are null
            DatabaseReference reference = databaseReference.child(Objects.requireNonNull(user.getPhoneNumber()));
            reference.child("haveData").get().addOnSuccessListener(dataSnapshot -> { // Call if already have data

                haveData = (Boolean) dataSnapshot.getValue();
                Log.i("ashis", "in getData-onSuccess have data =  " + haveData);


                if (haveData != null && haveData) {
                    Log.i("ashis", "entered in if");

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.i("ashis", "entered in onDataChange");

                            String storedName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                            if (storedName.equals("null"))
                                editOn = Operations.nameEdition("", binding.editTextProfileName, binding.imageViewEditname, true, ProfileActivity.this);
                            else
                                editOn = Operations.nameEdition(storedName, binding.editTextProfileName, binding.imageViewEditname, false, ProfileActivity.this);

                            userData.add(storedName);

                            String dpLink = (Objects.requireNonNull(snapshot.child("dp").getValue()).toString());
                            if (!dpLink.equals("null"))

                                Glide.with(ProfileActivity.this).load(dpLink).into(binding.profileCircleImageNewChat);
                            else
                                binding.profileCircleImageNewChat.setImageResource(R.drawable.ic_baseline_profile_black);
                            userData.add(dpLink);

                            String date = (Objects.requireNonNull(snapshot.child("DOJoining").getValue()).toString());
                            binding.textViewCreatedDate.setText(date);
                            userData.add(date);

                            binding.textViewPhNo.setText(Objects.requireNonNull(user.getPhoneNumber()).substring(3));

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.i("ashis", "entered in onCancelled");

                        }
                    });
                }


            }).addOnFailureListener(e -> {

                haveData = false;
                Log.i("ashis", "in getData-onFail have data =  " + false);

                editOn = Operations.nameEdition("", binding.editTextProfileName, binding.imageViewEditname, true, ProfileActivity.this);

                Glide.with(this).load(R.drawable.ic_baseline_profile_black).into(binding.profileCircleImageNewChat);

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        binding.textViewCreatedDate.setText(Objects.requireNonNull(snapshot.child("DOJoining").getValue()).toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

            });
        } catch (NullPointerException e) {
            Toast.makeText(this, "Can't make operation due to internal error", Toast.LENGTH_SHORT).show();
        }

    }

    private void profileImageClicked() {
        profileImageChooser();
    }

    private void profileImageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                dp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                Glide.with(this).load(imageUri).into(binding.profileCircleImageNewChat);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Selected image getting error", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onDestroy() {

        Bundle b = new Bundle();
        if(dp != null)
            b.putByteArray("dp", Operations.getByteArrayImage(dp));
        userData.remove(0);
        userData.add(0,binding.editTextProfileName.getText().toString().trim());
        b.putString("username",userData.get(0));
        try {
            Operations.updateDBData(user, databaseReference, storageReference, b);
        } catch (NullPointerException e) {
            Toast.makeText(this, "Can't make operation due to internal error", Toast.LENGTH_SHORT).show();
        }

        super.onDestroy();
    }
}