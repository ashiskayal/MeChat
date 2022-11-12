package com.kayalprints.mechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

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
    private TextView createdDateText;
    private ImageView nameEditIcon;
    private ConstraintLayout signOut;

//    private ArrayList<String> userData;
    private String[] userData;

    private boolean haveData = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Profile");
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profileCircleImage);
        name = findViewById(R.id.editTextProfileName);
        createdDateText = findViewById(R.id.textViewCreatedDate);
        nameEditIcon = findViewById(R.id.imageViewEditname);
        signOut = findViewById(R.id.signout);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("UsersData");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

//        userData = new ArrayList<>(3);
        userData = new String[3];

        getData();

        if(!haveData) {
            name.setClickable(true);
            nameEditIcon.setImageResource(R.drawable.ic_baseline_check);
            profileImage.setImageResource(R.drawable.ic_baseline_profile);
        } else {
            name.setClickable(false);
            name.setText(userData[0]);
            nameEditIcon.setImageResource(R.drawable.ic_baseline_edit);

            if(!userData[1].equals("null")) Picasso.get().load(userData[1]).into(profileImage);
            else profileImage.setImageResource(R.drawable.ic_baseline_profile);
        }
        createdDateText.setText(""+userData[2]); // Not working

        profileImage.setOnClickListener(v -> profileImageClicked());

        signOut.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(ProfileActivity.this, AuthenticationActivity.class));
            finish();
        });

    }


    private void getData() {
        FirebaseUser user = auth.getCurrentUser();

        if(user!=null) {
            databaseReference.child(user.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.i("ashis", "onDataChange");

                            userData[0] = (Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                            userData[1] = (Objects.requireNonNull(snapshot.child("dp").getValue()).toString());
                            userData[2] = (Objects.requireNonNull(snapshot.child("DOJoining").getValue()).toString());
                            Log.i("ashis","Getting Uid success " + userData[2]);

                            if (!(userData[0].equals("null"))) haveData = true;
                            else haveData = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.i("ashis", "onCancelled");
                        }
                    });
//                    .get().addOnCompleteListener(task -> {
//                        if(task.isSuccessful()) {
//                            DataSnapshot data = task.getResult();
//                            Log.i("ashis", "Task successful");
//
//                            userData.add(Objects.requireNonNull(data.child("name").getValue()).toString());
//                            userData.add(Objects.requireNonNull(data.child("dp").getValue()).toString());
//                            userData.add(Objects.requireNonNull(data.child("DOJoining").getValue()).toString());
//                            Toast.makeText(this, "Getting Uid success " + userData.get(2), Toast.LENGTH_SHORT).show();
//
//                            if (!(userData.get(0).equals("null"))) haveData = true;
//                            else haveData = false;
//                        } else {
//                            Log.i("ashis", "Task not successful");
//
//                        }
//
//                    })
//                    .addOnFailureListener(e -> {
//                        Log.i("ashis", "getting uid data failed");
//
//                        Toast.makeText(ProfileActivity.this, "Getting UID failed", Toast.LENGTH_SHORT).show();
//                    })
//                    .addOnCanceledListener(() -> {
//                        Log.i("ashis", "getting uid data canceled");
//
//                        Toast.makeText(ProfileActivity.this, "Getting UID canceled", Toast.LENGTH_SHORT).show();
//                    })
//                    .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//                        @Override
//                        public void onSuccess(DataSnapshot dataSnapshot) {
//                            Log.i("ashis", "onSuccess");
//
//                        }
//                    });
            Log.i("ashis", "or none hit");

        } else {
            Log.i("ashis", "user is null");
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
            Picasso.get().load(imageUri).into(profileImage);
            getProfileImageLink(imageUri);
        }

    }

    private String getProfileImageLink(Uri imageUri) {
        FirebaseUser user = auth.getCurrentUser();
        String link = "null";
        storageReference.child(user.getUid()).putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        link =
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Profile image upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
        return link;
    }
}