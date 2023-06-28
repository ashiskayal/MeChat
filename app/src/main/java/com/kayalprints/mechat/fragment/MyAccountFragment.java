package com.kayalprints.mechat.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.kayalprints.mechat.classes.AppFirebaseDatabase;
import com.kayalprints.mechat.classes.Operations;
import com.kayalprints.mechat.databinding.ActivityProfileBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MyAccountFragment extends Fragment {

    ActivityProfileBinding binding;

    private ArrayList<String> userData;
    private Bitmap dp;

    private boolean editOn;
    private Boolean haveData;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseUser user;

    private MyAccountFragment(DatabaseReference dReference, StorageReference sReference, FirebaseUser usr) {
        databaseReference = dReference;
        storageReference = sReference;
        user = usr;
    }

    public static MyAccountFragment getInstance(FirebaseDatabase database, FirebaseStorage storage, FirebaseAuth auth) {
        return new MyAccountFragment(database.getReference().child("UserData"), storage.getReference(), auth.getCurrentUser());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = ActivityProfileBinding.inflate(inflater);

        binding.signoutlay.setVisibility(View.INVISIBLE);

        DatabaseReference df = AppFirebaseDatabase.getDatabaseReference();
        if(df != null) databaseReference = df.child("UsersData");
        storageReference = AppFirebaseDatabase.getStorageReference(); // Can be null
        user = AppFirebaseDatabase.getCurrentUser(); // Can be null

        userData = new ArrayList<>(3);

        getData();

        binding.profileCircleImageNewChat.setOnClickListener(view -> profileImageClicked());

        binding.imageViewEditname.setOnClickListener(view -> {
            editOn = !editOn;
            String userName = binding.editTextProfileName.getText().toString().trim();
            Operations.nameEdition(userName, binding.editTextProfileName, binding.imageViewEditname, editOn, getContext());
        });


        return binding.getRoot();
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                dp = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                Glide.with(requireContext()).load(imageUri).into(binding.profileCircleImageNewChat);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Selected image getting error", Toast.LENGTH_SHORT).show();
            }
        }
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
                                editOn = Operations.nameEdition("", binding.editTextProfileName, binding.imageViewEditname, true, getContext());
                            else
                                editOn = Operations.nameEdition(storedName, binding.editTextProfileName, binding.imageViewEditname, false, getContext());

                            userData.add(storedName);

                            String dpLink = (Objects.requireNonNull(snapshot.child("dp").getValue()).toString());
                            if (!dpLink.equals("null"))
                                Glide.with(requireContext()).load(dpLink).into(binding.profileCircleImageNewChat);
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

                editOn = Operations.nameEdition("", binding.editTextProfileName, binding.imageViewEditname, true, getContext());

                binding.profileCircleImageNewChat.setImageResource(R.drawable.ic_baseline_profile_black);

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
            Toast.makeText(getContext(), "Can't make operation due to internal error", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onStart() {
        Log.i("fragDebug","OnStart");

        super.onStart();
    }

    @Override
    public void onStop() {
        Log.i("fragDebug","OnStop");

        super.onStop();
    }

    @Override
    public void onPause() {

        Log.i("fragDebug","OnPause");


        Bundle b = new Bundle();
        if(dp != null)
            b.putByteArray("dp", Operations.getByteArrayImage(dp));
        userData.remove(0);
        userData.add(0,binding.editTextProfileName.getText().toString().trim());
        b.putString("username",userData.get(0));
        try {
            Operations.updateDBData(user, databaseReference, storageReference, b);
        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "Can't make operation due to internal error", Toast.LENGTH_SHORT).show();
        }

        super.onPause();
    }
}
