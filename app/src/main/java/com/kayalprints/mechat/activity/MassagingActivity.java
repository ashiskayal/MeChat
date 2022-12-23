package com.kayalprints.mechat.activity;

import android.annotation.SuppressLint;
import android.app.Person;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kayalprints.mechat.R;
import com.kayalprints.mechat.adapter.PersonalMsgRVAdapter;
import com.kayalprints.mechat.classes.PersonalMassage;
import com.kayalprints.mechat.classes.User;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MassagingActivity extends AppCompatActivity {

    private RecyclerView rv;
    private EditText typedText;
    private ImageView sentIcon;
    private ProgressBar progressBar;

    private List<PersonalMassage> massageList;
    private String chatWithUser, currentUser;
    private DatabaseReference massageDatabaseReference;
    private PersonalMsgRVAdapter adapter;

    private String id;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userName = getIntent().getStringExtra("chatWithName");
        chatWithUser = getIntent().getStringExtra("chatWithPh");
        if(userName == null || userName.isEmpty() || userName.equals("null")) userName = chatWithUser;
        getSupportActionBar().setTitle(userName);

        setContentView(R.layout.activity_massaging);

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        if(currentUser.compareTo(chatWithUser) < 0)
            id = chatWithUser+currentUser;
        else id = currentUser+chatWithUser;

        massageDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Massages");

        rv = findViewById(R.id.personalChatRV);
        typedText = findViewById(R.id.personalChatEditText);
        sentIcon = findViewById(R.id.personalChatSentImage);
        progressBar = findViewById(R.id.progressBarPersonalChat);

        rv.setLayoutManager(new LinearLayoutManager(this));

        typedText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rv.scrollToPosition(massageList.size()-1);
            }
        });

        sentIcon.setOnClickListener(v -> {
            String massage = typedText.getText().toString().trim();

            if(!massage.isEmpty()) {
                massageDatabaseReference.child(id).child(String.valueOf(System.currentTimeMillis()))
                        .setValue(new PersonalMassage(massage, currentUser));
                typedText.setText("");
            }
        });

        getMassages();
    }

    private void getMassages() {
        massageList = new LinkedList<>();
        adapter = new PersonalMsgRVAdapter(massageList, currentUser);
        rv.setAdapter(adapter);

        DatabaseReference reference = massageDatabaseReference.child(id);

        reference.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                massageList.add(snapshot.getValue(PersonalMassage.class));

                adapter.notifyItemInserted(massageList.size()-1);
                rv.scrollToPosition(massageList.size()-1);

                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}