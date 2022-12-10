package com.kayalprints.mechat.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.kayalprints.mechat.fragment.AddChatFragment;
import com.kayalprints.mechat.R;
import com.kayalprints.mechat.classes.User;
import com.kayalprints.mechat.adapter.ChatsRVAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatsRv;
    private static ChatsRVAdapter adapter;

    private FloatingActionButton addChat;

    private ImageView allChats, addUser, myAccount;

    private static List<User> chats;

    private ActivityResultLauncher<Intent> activityResultLauncherForProfile;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("mainActivityCheck","OnCreate");

        if(checkPermission()) requestPermission();

        chats = new ArrayList<>();
        registerActivityLauncher();

        chatsRv = findViewById(R.id.recyclerViewChats);
        addChat = findViewById(R.id.floatingActionAddChat);
        allChats = findViewById(R.id.imageViewOptionsChats);
        addUser = findViewById(R.id.imageViewOptionsAddChat);
        myAccount = findViewById(R.id.imageViewOptionsAccount);

        chatsRv.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        adapter = new ChatsRVAdapter(chats,MainActivity.this);
        chatsRv.setAdapter(adapter);

        addChat.setImageResource(R.drawable.ic_baseline_person_add_white);

        allChats.setOnClickListener(v -> changeImageOpacity(allChats, addUser, myAccount));
        addUser.setOnClickListener(v -> changeImageOpacity(addUser, allChats, myAccount));
        myAccount.setOnClickListener(v -> changeImageOpacity(myAccount, allChats, addUser));


        addChat.setOnClickListener(v -> {
            // Code to change the constraint values
            ConstraintLayout constraintLayout = findViewById(R.id.mainLayout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.optionsLayout,ConstraintSet.TOP,R.id.mainLayout,ConstraintSet.TOP,0);
            constraintSet.applyTo(constraintLayout);
            //--------------

            FragmentManager fm = getSupportFragmentManager();
            DialogFragment fragment = new AddChatFragment(FirebaseDatabase.getInstance().getReference().child("UsersData"));
            fragment.show(fm, "AddChatFragment");
        });

//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        if(imm.isAcceptingText()) Log.i("mainActivityCheck","keyboard is on.");
//        else Log.i("mainActivityCheck","keyboard is not on.");

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
                activityResultLauncherForProfile.launch(i);
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
    private void registerActivityLauncher() {
        activityResultLauncherForProfile = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if (result.getData() != null && result.getResultCode() == RESULT_OK) {
                            startActivity(new Intent(MainActivity.this, AuthenticationActivity.class));
                            finish();
                        }
                    }
                });
    }

    private void createFile(Path path) {
        File file = path.toFile();
        if (file.mkdir()) // createNewFile() to create new file
            Log.i("mainActivityCheck", "File created in " + file.getAbsolutePath());
        else Log.i("mainActivityCheck", "File already present");
    }

    private void requestPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            && ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Returns true if read and write permission is not granted
            // If not granted the permissions then user have to give the permissions from the setting
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    100);
            createFile(Paths.get(Environment.getExternalStorageDirectory().getPath(),"/meChat"));
        }
    }

    private boolean checkPermission() {
        int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (readPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED);
    }


    @SuppressLint("NotifyDataSetChanged")
    public static void updateChatList(User user) {
        chats.add(0,user);
        adapter.notifyDataSetChanged();
    }

    private void changeImageOpacity(ImageView selectedImage, ImageView... anotherImages) {
        selectedImage.animate().alpha(1).setDuration(500).start();
        for(ImageView i : anotherImages) i.animate().alpha(0.5F).setDuration(500).start();
    }

    @Override
    protected void onStart() {
        Log.i("mainActivityCheck","OnStart");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.i("mainActivityCheck","OnPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i("mainActivityCheck","OnResume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.i("mainActivityCheck","onRestart");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.i("mainActivityCheck","OnDestroy");



        super.onDestroy();
    }
}