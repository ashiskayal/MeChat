package com.kayalprints.mechat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
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

    private CircleImageView profileImage;
    private EditText name;
    private TextView createdDateText, phoneNoText;
    private ImageView nameEditIcon;
    private ConstraintLayout signOut, userNameLay;
    private ProgressBar progressbarDp;

    private ArrayList<String> userData;
    private Bitmap dp;

    private boolean haveData, editOn;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Profile");
        setContentView(R.layout.activity_profile);

        haveData = getIntent().getBooleanExtra("haveData", false);

        profileImage = findViewById(R.id.profileCircleImage);
        name = findViewById(R.id.editTextProfileName);
        createdDateText = findViewById(R.id.textViewCreatedDate);
        nameEditIcon = findViewById(R.id.imageViewEditname);
        signOut = findViewById(R.id.signoutlay);
        userNameLay = findViewById(R.id.userNameLay);
        progressbarDp = findViewById(R.id.progressbarDp);
        phoneNoText = findViewById(R.id.textViewPhNo);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("UsersData");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        userData = new ArrayList<>(3);

        getData();

        profileImage.setOnClickListener(v -> profileImageClicked());

        nameEditIcon.setOnClickListener(v -> {
            String userName = name.getText().toString().trim();
            editOn = StaticOperations.nameEdition(userName, name, nameEditIcon, editOn, ProfileActivity.this);
        });

        signOut.setOnClickListener(v -> {
            auth.signOut();
            setResult(RESULT_OK, null);
            finish();
        });

    }


    private void getData() {
        FirebaseUser user = auth.getCurrentUser();

        if(user!=null) {
            if(haveData) {
                databaseReference.child(Objects.requireNonNull(user.getPhoneNumber()))
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                userData.add((Objects.requireNonNull(snapshot.child("name").getValue()).toString()));
                                userData.add((Objects.requireNonNull(snapshot.child("dp").getValue()).toString()));
                                userData.add((Objects.requireNonNull(snapshot.child("DOJoining").getValue()).toString()));

                                if (!(userData.get(0).equals("null"))) haveData = true;

                                updateData();  // Update should be called after all the data fetched from the server because server needs some time to get the data.
                                editOn = StaticOperations.nameEdition(userData.get(0), name, nameEditIcon, haveData, ProfileActivity.this);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
            } else {
                name.setEnabled(true);
                nameEditIcon.setImageResource(R.drawable.ic_baseline_check);
                profileImage.setImageResource(R.drawable.ic_baseline_profile);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateData() {
        if(!haveData) {
            name.setEnabled(true);
            nameEditIcon.setImageResource(R.drawable.ic_baseline_check);
        } else {
            name.setClickable(false);
            name.setEnabled(false);
            name.setText(userData.get(0));
            nameEditIcon.setImageResource(R.drawable.ic_baseline_edit);
        }
        if(!userData.get(1).equals("null")) {
            new Thread(() -> {
                try {
                    dp = Picasso.get().load(userData.get(1)).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            Picasso.get().load(userData.get(1)).into(profileImage);
        }
        else profileImage.setImageResource(R.drawable.ic_baseline_profile);
        progressbarDp.setVisibility(View.INVISIBLE);
        createdDateText.setText(""+userData.get(2));

        String n = auth.getCurrentUser().getPhoneNumber();
        phoneNoText.setText(n.substring(3));

//        visibleComponents();
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

        StaticOperations.updateDBData(auth.getCurrentUser(), databaseReference, storageReference, b);

        super.onDestroy();
    }
}