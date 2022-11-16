package com.kayalprints.mechat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
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
    private Button btnContinue;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private boolean editOn, haveData;
    private String userName;
    private Bitmap dp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_data_set_once);

        profileImage = findViewById(R.id.profileCircleImageOnce);
        name = findViewById(R.id.editTextProfileNameOnce);
        createdDateText = findViewById(R.id.textViewCreatedDateOnce);
        nameEditIcon = findViewById(R.id.imageViewEditnameOnce);
        userNameLay = findViewById(R.id.userNameLayOnce);
        btnContinue = findViewById(R.id.buttonContinue);
        btnContinue.setBackgroundResource(R.drawable.ic_baseline_right);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("UsersData");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Intent i = getIntent();
        haveData = i.getBooleanExtra("haveData",false);
        getData(i);

        nameEditIcon.setOnClickListener(v -> {
            userName = name.getText().toString().trim();
            editOn = StaticOperations.nameEdition(userName, name, nameEditIcon, editOn, ProfileDataSetOnceActivity.this);
        });

        profileImage.setOnClickListener(v -> profileImageClicked());

        btnContinue.setOnClickListener(v -> {
            if(editOn || userName == null) userName = name.getText().toString().trim();
            Intent intent = new Intent(ProfileDataSetOnceActivity.this, MainActivity.class);
            if(!userName.isEmpty()) haveData = true;
            intent.putExtra("haveData",haveData);
            startActivity(intent);
            finish();
        });

    }


    private void getData(Intent i) {
        if(haveData) {
            FirebaseUser user = auth.getCurrentUser();

            if(user!=null) {
                databaseReference.child(Objects.requireNonNull(user.getPhoneNumber()))
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                name.setEnabled(false);
                                name.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                                String dpLink = (Objects.requireNonNull(snapshot.child("dp").getValue()).toString());
                                if(!dpLink.equals("null"))
                                    Picasso.get().load(dpLink).into(profileImage);
                                else profileImage.setImageResource(R.drawable.ic_baseline_profile);

                                createdDateText.setText(i.getStringExtra("createdDate"));

                                StaticOperations.nameEdition(name.getText().toString(), name, nameEditIcon, true, ProfileDataSetOnceActivity.this);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
            }

            return;
        }
        name.setEnabled(true);
        nameEditIcon.setImageResource(R.drawable.ic_baseline_check);
        editOn = true;
        profileImage.setImageResource(R.drawable.ic_baseline_profile);
        createdDateText.setText(i.getStringExtra("createdDate"));
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
            b.putString("username", userName);

            StaticOperations.updateDBData(auth.getCurrentUser(), databaseReference, storageReference, b);

            super.onDestroy();
        }
    }
}