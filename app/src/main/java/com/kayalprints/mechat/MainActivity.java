package com.kayalprints.mechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_FINISH = 98;

    private RecyclerView chatsRv;
    private FloatingActionButton addChat;

    private List<User> chats;

    private boolean haveData;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        haveData = getIntent().getBooleanExtra("haveData", false);

        chats = new ArrayList<>();

        chatsRv = findViewById(R.id.recyclerViewChats);
        addChat = findViewById(R.id.floatingActionAddChat);

        chatsRv.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        ChatsRVAdapter adapter = new ChatsRVAdapter(chats,MainActivity.this);
        chatsRv.setAdapter(adapter);


        addChat.setOnClickListener(v -> {
            String newUserPhNumber = "";
            addNewUser(newUserPhNumber);
        });

    }

    private void addNewUser(String phone) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.landingpagemenu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_profile:
                Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                i.putExtra("haveData", haveData);
                startActivityForResult(i, REQUEST_FINISH);
                return true;
            case R.id.menu_setting:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                return true;
            case R.id.app_bar_search:
                // Perform the search operation here
                return true;
            default:return super.onOptionsItemSelected(item);
        }

    }

    /** This is used to finish this activity when log out from other activities */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FINISH && resultCode == RESULT_OK) {
            startActivity(new Intent(MainActivity.this, AuthenticationActivity.class));
            finish();
        }

    }
}