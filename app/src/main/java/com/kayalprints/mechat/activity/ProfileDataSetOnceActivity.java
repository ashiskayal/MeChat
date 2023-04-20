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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kayalprints.mechat.R;
import com.kayalprints.mechat.classes.Operations;
import com.kayalprints.mechat.databinding.ActivityProfileDataSetOnceBinding;

import java.io.IOException;
import java.util.Objects;

public class ProfileDataSetOnceActivity extends AppCompatActivity {

    ActivityProfileDataSetOnceBinding binding;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseUser user;

    private boolean editOn;
    private Boolean haveData;
    private String userName;
    private Bitmap dp;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileDataSetOnceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Glide.with(this).load(R.drawable.ic_baseline_right).into(binding.imageContinue);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("UsersData");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        user = auth.getCurrentUser();

        getData();

        binding.imageViewEditnameOnce.setOnClickListener(v -> {
            editOn = !editOn;
            userName = binding.editTextProfileNameOnce.getText().toString().trim();
            Operations.nameEdition(userName, binding.editTextProfileNameOnce, binding.imageViewEditnameOnce, editOn, ProfileDataSetOnceActivity.this);
        });

        binding.profileCircleImageOnce.setOnClickListener(v -> profileImageClicked());

        binding.imageContinue.setOnClickListener(v -> {
            if(editOn || userName == null || userName.isEmpty()) userName = binding.editTextProfileNameOnce.getText().toString().trim();
            startActivity(new Intent(ProfileDataSetOnceActivity.this, MainActivity.class));
            finish();
        });
    }


    private void getData() {

        DatabaseReference reference = databaseReference.child(Objects.requireNonNull(user.getPhoneNumber()));
        reference.child("haveData").get().addOnSuccessListener(dataSnapshot -> { // Called if already have data

            haveData = (Boolean) dataSnapshot.getValue();
            Log.i("ashis", "in getData-onSuccess have data =  "+haveData);


            if(haveData != null && haveData) {
                reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                String storedName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                if(storedName.equals("null"))
                                    editOn = Operations.nameEdition("", binding.editTextProfileNameOnce, binding.imageViewEditnameOnce, true, ProfileDataSetOnceActivity.this);
                                else
                                    editOn = Operations.nameEdition(storedName, binding.editTextProfileNameOnce, binding.imageViewEditnameOnce, false, ProfileDataSetOnceActivity.this);


                                String dpLink = (Objects.requireNonNull(snapshot.child("dp").getValue()).toString());
                                if(!dpLink.equals("null"))
                                    Glide.with(ProfileDataSetOnceActivity.this).load(dpLink).into(binding.profileCircleImageOnce);
                                else Glide.with(ProfileDataSetOnceActivity.this).load(R.drawable.ic_baseline_profile_black).into(binding.profileCircleImageOnce);

                                binding.textViewCreatedDateOnce.setText(Objects.requireNonNull(snapshot.child("DOJoining").getValue()).toString());

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.i("ashis", "onCancelled in getData");
                            }
                        });
            }


        }).addOnFailureListener(e -> {

            haveData = false;
            Log.i("ashis", "in getData-onFail have data =  "+false);


            editOn = Operations.nameEdition("",binding.editTextProfileNameOnce,binding.imageViewEditnameOnce,true,ProfileDataSetOnceActivity.this);

            Glide.with(this).load(R.drawable.ic_baseline_profile_black).into(binding.profileCircleImageOnce);

            reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            binding.textViewCreatedDateOnce.setText(Objects.requireNonNull(snapshot.child("DOJoining").getValue()).toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });


        });

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
                Glide.with(ProfileDataSetOnceActivity.this).load(imageUri).into(binding.profileCircleImageOnce);
                haveData = true;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Selected image getting error", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onDestroy() {
        Bundle b = new Bundle();
        if (dp != null) {
            b.putByteArray("dp", Operations.getByteArrayImage(dp));
            userName = binding.editTextProfileNameOnce.getText().toString().trim();
            b.putString("username", userName);

            Log.i("ashis","in destroy : "+b.getString("username"));
            Operations.updateDBData(user, databaseReference, storageReference, b);
        }
        super.onDestroy();
    }
}