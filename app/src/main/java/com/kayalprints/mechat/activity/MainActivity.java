package com.kayalprints.mechat.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.kayalprints.mechat.R;
import com.kayalprints.mechat.classes.MeChatDatabase;
import com.kayalprints.mechat.fragment.AddChatFragment;
import com.kayalprints.mechat.fragment.ChatsFragment;
import com.kayalprints.mechat.fragment.MyAccountFragment;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int ALLCHATS = 35, ADDUSER = 36, MYACCOUNT = 37;
    private static int currentFrag;

    private FloatingActionButton addChat;

    private ImageView allChats, addUser, myAccount;
    private FragmentContainerView containerView;

    private ActivityResultLauncher<Intent> activityResultLauncherForProfile;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("mainActivityCheck","OnCreate");

        MeChatDatabase.setMeChatDatabase(FirebaseDatabase.getInstance(), FirebaseStorage.getInstance(), FirebaseAuth.getInstance());

        if(checkPermission()) requestPermission();

//        ChatDataHolder.addChat(new User("Default User2", "null", "8888888888"));

        registerActivityLauncher();


        addChat = findViewById(R.id.floatingActionAddChat);
        allChats = findViewById(R.id.imageViewOptionsChats);
        addUser = findViewById(R.id.imageViewOptionsAddChat);
        myAccount = findViewById(R.id.imageViewOptionsAccount);
        containerView = findViewById(R.id.mainFragmentContainerView);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ChatsFragment chatsFragment = new ChatsFragment();

        ft.add(R.id.mainFragmentContainerView,chatsFragment);
        ft.commit();

        MainActivity.currentFrag = MainActivity.ALLCHATS;

        allChats.setOnClickListener(v -> {
            if(MainActivity.currentFrag != MainActivity.ALLCHATS) {
                changeImageOpacity(allChats, addUser, myAccount);

                ChatsFragment fragment = new ChatsFragment();

                FragmentManager manager = getSupportFragmentManager();
                showFragment(manager, "chats", fragment, R.id.mainFragmentContainerView);
                hideFragments(manager, "myaccount", "adduser");

                MainActivity.currentFrag = MainActivity.ALLCHATS;
            }
        });

        addUser.setOnClickListener(v -> {
            if(MainActivity.currentFrag != MainActivity.ADDUSER) {
                changeImageOpacity(addUser, allChats, myAccount);

                AddChatFragment fragment = AddChatFragment.getInstance(FirebaseDatabase.getInstance().getReference());

                FragmentManager manager = getSupportFragmentManager();
                showFragment(manager, "adduser", fragment, R.id.mainFragmentContainerView);
                hideFragments(manager, "myaccount", "chats");

                MainActivity.currentFrag = MainActivity.ADDUSER;
            }
        });

        myAccount.setOnClickListener(v -> {
            if(MainActivity.currentFrag != MainActivity.MYACCOUNT) {
                changeImageOpacity(myAccount, allChats, addUser);

                MyAccountFragment fragment = MyAccountFragment.getInstance(
                        FirebaseDatabase.getInstance(), FirebaseStorage.getInstance(), FirebaseAuth.getInstance()
                );

                FragmentManager manager = getSupportFragmentManager();
                showFragment(manager, "myaccount", fragment, R.id.mainFragmentContainerView);
                hideFragments(manager, "adduser", "chats");

                MainActivity.currentFrag = MainActivity.MYACCOUNT;
            }
        });

/**
        addChat.setOnClickListener(v -> {
            // Code to change the constraint values
            ConstraintLayout constraintLayout = findViewById(R.id.mainLayout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.optionsLayout,ConstraintSet.TOP,R.id.mainLayout,ConstraintSet.TOP,0);
            constraintSet.applyTo(constraintLayout);
            //--------------

            DialogFragment fragment = new AddChatFragment(FirebaseDatabase.getInstance().getReference().child("UsersData"));
            fragment.show(getSupportFragmentManager(), "AddChatFragment");
        });
**/
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

    private void changeImageOpacity(ImageView selectedImage, ImageView... anotherImages) {
        selectedImage.animate().alpha(1).setDuration(500).start();
        for(ImageView i : anotherImages) i.animate().alpha(0.5F).setDuration(500).start();
    }


    private void showFragment(FragmentManager manager, String fragmentTag, Fragment fragment, @IdRes int containerViewId) {
        if(manager.findFragmentByTag(fragmentTag) != null) {
            manager.beginTransaction()
                    .show(Objects.requireNonNull(manager.findFragmentByTag(fragmentTag))).commit();
        } else manager.beginTransaction().add(containerViewId,fragment, fragmentTag).commit();
    }

    private void hideFragments(FragmentManager manager, String tag1, String tag2) {
        if(manager.findFragmentByTag(tag1) != null)
            manager.beginTransaction()
                    .hide(Objects.requireNonNull(manager.findFragmentByTag(tag1))).commit();

        if(manager.findFragmentByTag(tag2) != null)
            manager.beginTransaction()
                    .hide(Objects.requireNonNull(manager.findFragmentByTag(tag2))).commit();
    }

}