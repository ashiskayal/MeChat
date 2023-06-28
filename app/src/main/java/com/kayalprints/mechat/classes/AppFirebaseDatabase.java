package com.kayalprints.mechat.classes;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AppFirebaseDatabase {
    static FirebaseDatabase database;
    static FirebaseStorage storage;
    static FirebaseAuth auth;

    public static void setMeChatDatabase(FirebaseDatabase database, FirebaseStorage storage, FirebaseAuth auth) {
        AppFirebaseDatabase.database = database;
        AppFirebaseDatabase.storage = storage;
        AppFirebaseDatabase.auth = auth;
    }

    public static FirebaseDatabase getDatabase() {
        return database;
    }

    public static FirebaseStorage getStorage() {
        return storage;
    }

    public static FirebaseAuth getAuth() {
        return auth;
    }

    @Nullable
    public static DatabaseReference getDatabaseReference() {
        if(database != null)
            return database.getReference();
        return null;
    }

    @Nullable
    public static StorageReference getStorageReference() {
        if(storage != null)
            return storage.getReference();
        return null;
    }

    @Nullable
    public static FirebaseUser getCurrentUser() {
        if(auth != null)
            return auth.getCurrentUser();
        return null;
    }

}
