package com.kayalprints.mechat.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kayalprints.mechat.adapter.PersonalMsgRVAdapter;
import com.kayalprints.mechat.classes.PersonalMassage;
import com.kayalprints.mechat.databinding.ActivityMassagingBinding;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class MassagingActivity extends AppCompatActivity {

    private ActivityMassagingBinding binding;

    private List<PersonalMassage> massageList;
    private String chatWithUser, currentUser;
    private DatabaseReference massageDatabaseReference;
    private PersonalMsgRVAdapter adapter;

    private String id;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMassagingBinding.inflate(getLayoutInflater());

        String userName = getIntent().getStringExtra("chatWithName");
        chatWithUser = getIntent().getStringExtra("chatWithPh");
        if(userName == null || userName.isEmpty() || userName.equals("null")) userName = chatWithUser;
        Objects.requireNonNull(getSupportActionBar()).setTitle(userName);

        setContentView(binding.getRoot());

        currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();

        assert currentUser != null;
        if(currentUser.compareTo(chatWithUser) < 0)
            id = chatWithUser+currentUser;
        else id = currentUser+chatWithUser;

        massageDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Massages");

        binding.personalChatRV.setLayoutManager(new LinearLayoutManager(this));

        binding.personalChatEditText.setOnClickListener(v -> binding.personalChatRV.scrollToPosition(massageList.size()-1));

        binding.personalChatSentImage.setOnClickListener(v -> {
            String massage = binding.personalChatEditText.getText().toString().trim();

            if(!massage.isEmpty()) {
                massageDatabaseReference.child(id).child(String.valueOf(System.currentTimeMillis()))
                        .setValue(new PersonalMassage(massage, currentUser));
                binding.personalChatEditText.setText("");
            }
        });

        getMassages();
    }

    private void getMassages() {
        massageList = new LinkedList<>();
        adapter = new PersonalMsgRVAdapter(massageList, currentUser);
        binding.personalChatRV.setAdapter(adapter);

        DatabaseReference reference = massageDatabaseReference.child(id);

        reference.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                massageList.add(snapshot.getValue(PersonalMassage.class));

                adapter.notifyItemInserted(massageList.size()-1);
                binding.personalChatRV.scrollToPosition(massageList.size()-1);

                binding.progressBarPersonalChat.setVisibility(View.INVISIBLE);
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
                binding.progressBarPersonalChat.setVisibility(View.INVISIBLE);
            }
        });
    }
}