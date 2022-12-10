package com.kayalprints.mechat.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import com.kayalprints.mechat.classes.StaticOperations;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseUser user;

    private CircleImageView profileImage;
    private EditText name;
    private TextView createdDateText, phoneNoText;
    private ImageView nameEditIcon;
    private ConstraintLayout signOut, userNameLay;
    private ProgressBar progressbarDp;

    private ArrayList<String> userData;
    private Bitmap dp;

    private boolean editOn;
    private Boolean haveData;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profileCircleImageNewChat);
        name = findViewById(R.id.editTextProfileName);
        createdDateText = findViewById(R.id.textViewCreatedDate);
        nameEditIcon = findViewById(R.id.imageViewEditname);
        signOut = findViewById(R.id.signoutlay);
        userNameLay = findViewById(R.id.userNameLay);
        progressbarDp = findViewById(R.id.progressbarDpNewChat);
        phoneNoText = findViewById(R.id.textViewPhNo);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("UsersData");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        user = auth.getCurrentUser();

        userData = new ArrayList<>(3);

        getData();

        profileImage.setOnClickListener(v -> profileImageClicked());

        nameEditIcon.setOnClickListener(v -> {
            editOn = !editOn;
            String userName = name.getText().toString().trim();
            StaticOperations.nameEdition(userName, name, nameEditIcon, editOn, ProfileActivity.this);
        });

        signOut.setOnClickListener(v -> {
            auth.signOut();
            setResult(RESULT_OK, new Intent());
            finish();
        });

    }


    private void getData() {

        assert user != null;
        DatabaseReference reference = databaseReference.child(Objects.requireNonNull(user.getPhoneNumber()));
        reference.child("haveData").get().addOnSuccessListener(dataSnapshot -> { // Called if already have data

            haveData = (Boolean) dataSnapshot.getValue();
            Log.i("ashis", "in getData-onSuccess have data =  "+haveData);


            if(haveData != null && haveData) {
                Log.i("ashis", "entered in if");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.i("ashis", "entered in onDataChange");

                        String storedName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                        if(storedName.equals("null"))
                            editOn = StaticOperations.nameEdition("", name, nameEditIcon, true, ProfileActivity.this);
                        else
                            editOn = StaticOperations.nameEdition(storedName, name, nameEditIcon, false, ProfileActivity.this);

                        userData.add(storedName);

                        String dpLink = (Objects.requireNonNull(snapshot.child("dp").getValue()).toString());
                        if(!dpLink.equals("null"))
                            Picasso.get().load(dpLink).into(profileImage);
                        else profileImage.setImageResource(R.drawable.ic_baseline_profile_black);
                        userData.add(dpLink);

                        String date = (Objects.requireNonNull(snapshot.child("DOJoining").getValue()).toString());
                        createdDateText.setText(date);
                        userData.add(date);

                        phoneNoText.setText(Objects.requireNonNull(user.getPhoneNumber()).substring(3));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i("ashis", "entered in onCancelled");

                    }
                });
            }



        }).addOnFailureListener(e -> {

            haveData = false;
            Log.i("ashis", "in getData-onFail have data =  "+ false);

            editOn = StaticOperations.nameEdition("",name,nameEditIcon,true,ProfileActivity.this);

            profileImage.setImageResource(R.drawable.ic_baseline_profile_black);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    createdDateText.setText(Objects.requireNonNull(snapshot.child("DOJoining").getValue()).toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        });


        Log.i("ashis", "after if");

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
                Picasso.get().load(imageUri).into(profileImage);
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
            b.putByteArray("dp",StaticOperations.getByteArrayImage(dp));
        userData.remove(0);
        userData.add(0,name.getText().toString().trim());
        b.putString("username",userData.get(0));

        StaticOperations.updateDBData(user, databaseReference, storageReference, b);

        super.onDestroy();
    }
}