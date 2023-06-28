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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.kayalprints.mechat.R;
import com.kayalprints.mechat.classes.AppFirebaseDatabase;
import com.kayalprints.mechat.databinding.ActivityMainBinding;
import com.kayalprints.mechat.fragment.AddChatFragment;
import com.kayalprints.mechat.fragment.ChatsFragment;
import com.kayalprints.mechat.fragment.MyAccountFragment;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String ALL_CHATS_TAG = "chats", ADD_USER_TAG = "adduser", MY_ACCOUNT_TAG = "myaccount";

    private ActivityMainBinding binding;

    private ActivityResultLauncher<Intent> activityResultLauncherForProfile;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppFirebaseDatabase.setMeChatDatabase(FirebaseDatabase.getInstance(), FirebaseStorage.getInstance(), FirebaseAuth.getInstance());

        if(checkPermission()) requestPermission();

        registerActivityLauncher();

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        ChatsFragment chatsFragment = new ChatsFragment();
        AddChatFragment addChatFragment = AddChatFragment.getInstance(FirebaseDatabase.getInstance().getReference());
        MyAccountFragment myAccountFragment = MyAccountFragment.getInstance(
                FirebaseDatabase.getInstance(), FirebaseStorage.getInstance(), FirebaseAuth.getInstance()
        );


        // Adding all the available fragments
        ft.add(R.id.mainFragmentContainerView,chatsFragment,ALL_CHATS_TAG);
        ft.add(R.id.mainFragmentContainerView,addChatFragment, ADD_USER_TAG);
        ft.add(R.id.mainFragmentContainerView,myAccountFragment, MY_ACCOUNT_TAG);
        ft.commitNow();

        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.hide(Objects.requireNonNull(manager.findFragmentByTag(MY_ACCOUNT_TAG)));
        fragmentTransaction.hide(Objects.requireNonNull(manager.findFragmentByTag(ADD_USER_TAG)));
        fragmentTransaction.show(Objects.requireNonNull(manager.findFragmentByTag(ALL_CHATS_TAG)));
        fragmentTransaction.commit();

        binding.imageViewOptionsChats.setOnClickListener(v -> {
                changeImageOpacity(binding.imageViewOptionsChats, binding.imageViewOptionsAddChat, binding.imageViewOptionsAccount);

                FragmentTransaction transaction = manager.beginTransaction();
                try { // If myAccount and AddUser fragment is not added to the fragment manager.
                    transaction.hide(Objects.requireNonNull(manager.findFragmentByTag(MY_ACCOUNT_TAG)));
                    transaction.hide(Objects.requireNonNull(manager.findFragmentByTag(ADD_USER_TAG)));
                } catch (NullPointerException ignored) {}

                try {
                    transaction.show(Objects.requireNonNull(manager.findFragmentByTag(ALL_CHATS_TAG)));
                } catch (NullPointerException e) {
                    transaction.add(R.id.mainFragmentContainerView,chatsFragment,ALL_CHATS_TAG);
                } finally {
                    transaction.commit();
                }
        });

        binding.imageViewOptionsAddChat.setOnClickListener(v -> {
                changeImageOpacity(binding.imageViewOptionsAddChat, binding.imageViewOptionsChats, binding.imageViewOptionsAccount);

                FragmentTransaction transaction = manager.beginTransaction();
                try {
                    transaction.hide(Objects.requireNonNull(manager.findFragmentByTag(ALL_CHATS_TAG)));
                    transaction.hide(Objects.requireNonNull(manager.findFragmentByTag(MY_ACCOUNT_TAG)));
                } catch (NullPointerException ignored) {}

                try {
                    transaction.show(Objects.requireNonNull(manager.findFragmentByTag(ADD_USER_TAG)));
                } catch (NullPointerException e) {
                    transaction.add(R.id.mainFragmentContainerView,addChatFragment, ADD_USER_TAG);
                } finally {
                    transaction.commit();
                }
        });

        binding.imageViewOptionsAccount.setOnClickListener(v -> {
                changeImageOpacity(binding.imageViewOptionsAccount, binding.imageViewOptionsChats, binding.imageViewOptionsAddChat);

                FragmentTransaction transaction = manager.beginTransaction();
                try {
                    transaction.hide(Objects.requireNonNull(manager.findFragmentByTag(ALL_CHATS_TAG)));
                    transaction.hide(Objects.requireNonNull(manager.findFragmentByTag(ADD_USER_TAG)));
                } catch (NullPointerException ignored) {}

                try {
                    transaction.show(Objects.requireNonNull(manager.findFragmentByTag(MY_ACCOUNT_TAG)));
                } catch (NullPointerException e) {
                    transaction.add(R.id.mainFragmentContainerView,myAccountFragment, MY_ACCOUNT_TAG);
                } finally {
                    transaction.commit();
                }
        });
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

    /* This is used to finish this activity when log out from other activities */
    private void registerActivityLauncher() {
        activityResultLauncherForProfile = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null && result.getResultCode() == RESULT_OK) {
                        startActivity(new Intent(MainActivity.this, AuthenticationActivity.class));
                        finish();
                    }
                });
    }

    private void createFile(@NonNull Path path) {
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

    private void changeImageOpacity(@NonNull ImageView selectedImage, @NonNull ImageView... anotherImages) {
        selectedImage.animate().alpha(1).setDuration(500).start();
        for(ImageView i : anotherImages) i.animate().alpha(0.5F).setDuration(500).start();
    }
}