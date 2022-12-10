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
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileDataSetOnceActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private EditText name;
    private TextView createdDateText;
    private ImageView nameEditIcon;
    private ConstraintLayout userNameLay;
    private ImageView imgContinue;

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
        setContentView(R.layout.activity_profile_data_set_once);

        profileImage = findViewById(R.id.profileCircleImageOnce);
        name = findViewById(R.id.editTextProfileNameOnce);
        createdDateText = findViewById(R.id.textViewCreatedDateOnce);
        nameEditIcon = findViewById(R.id.imageViewEditnameOnce);
        userNameLay = findViewById(R.id.userNameLayOnce);
        imgContinue = findViewById(R.id.imageContinue);
        imgContinue.setBackgroundResource(R.drawable.ic_baseline_right);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("UsersData");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        user = auth.getCurrentUser();

        getData();

        nameEditIcon.setOnClickListener(v -> {
            editOn = !editOn;
            userName = name.getText().toString().trim();
            StaticOperations.nameEdition(userName, name, nameEditIcon, editOn, ProfileDataSetOnceActivity.this);
        });

        profileImage.setOnClickListener(v -> profileImageClicked());

        imgContinue.setOnClickListener(v -> {
            if(editOn || userName == null || userName.isEmpty()) userName = name.getText().toString().trim();
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
                                    editOn = StaticOperations.nameEdition("", name, nameEditIcon, true, ProfileDataSetOnceActivity.this);
                                else
                                    editOn = StaticOperations.nameEdition(storedName, name, nameEditIcon, false, ProfileDataSetOnceActivity.this);


                                String dpLink = (Objects.requireNonNull(snapshot.child("dp").getValue()).toString());
                                if(!dpLink.equals("null"))
                                    Picasso.get().load(dpLink).into(profileImage);
                                else profileImage.setImageResource(R.drawable.ic_baseline_profile_black);

                                createdDateText.setText(Objects.requireNonNull(snapshot.child("DOJoining").getValue()).toString());

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


            editOn = StaticOperations.nameEdition("",name,nameEditIcon,true,ProfileDataSetOnceActivity.this);

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
            b.putByteArray("dp", StaticOperations.getByteArrayImage(dp));
            userName = name.getText().toString().trim();
            b.putString("username", userName);

            Log.i("ashis","in destroy : "+b.getString("username"));
            StaticOperations.updateDBData(user, databaseReference, storageReference, b);
        }
        super.onDestroy();
    }
}