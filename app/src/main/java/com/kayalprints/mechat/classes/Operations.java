package com.kayalprints.mechat.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.kayalprints.mechat.R;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Operations {

    private Operations() {}

    private static String imageLink;

    public static String getImageLink() {
        return imageLink;
    }

//    public static String getProfileImageLink(Uri imageUri, FirebaseUser user, StorageReference storageReference, Context context) {
//        String link = "null";
//        storageReference.child(user.getUid()).putFile(imageUri)
//                .addOnSuccessListener(taskSnapshot -> {
////                        link =
//                })
//                .addOnFailureListener(e -> Toast.makeText(context, "Profile image upload failed", Toast.LENGTH_SHORT).show());
//        return link;
//    }

    public static void updateDBData(FirebaseUser user, DatabaseReference databaseReference, StorageReference storageReference, Bundle data) {
        if(user != null) {
            boolean dpNull = false;
            if(data.getByteArray("dp") == null) dpNull = true;
            Log.i("ashis", "dpNull = "+dpNull);
            if(!dpNull) {

                byte[] image = data.getByteArray("dp");
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);


                storageReference.child(user.getPhoneNumber()).child("profileImage")
                        .child("40%")
                        .putBytes(scalingImage(imageBitmap,imageBitmap.getWidth()/2, 40));

                storageReference.child(user.getPhoneNumber()).child("profileImage")
                        .child("20%")
                        .putBytes(scalingImage(imageBitmap,imageBitmap.getWidth()/4, 20));

                storageReference.child(user.getPhoneNumber()).child("profileImage")
                        .child("dp1")
                        .putBytes(scalingImage(imageBitmap,imageBitmap.getWidth()/10, 10));

                storageReference.child(Objects.requireNonNull(user.getPhoneNumber())).child("profileImage")
                        .child("100%")
                        .putBytes(image)
                        .addOnSuccessListener(taskSnapshot -> {

                            StorageReference imageStorageReference = storageReference.child(user.getPhoneNumber()).child("profileImage").child("dp1");
                            imageStorageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                imageLink = uri.toString();
                                databaseReference.child(user.getPhoneNumber()).child("dp").setValue(imageLink);

                            }).addOnFailureListener(e -> Log.i("ashis", e.toString()));
                            Log.i("ashis", "putting image success");

                            // Create a notification here
                        })
                        .addOnFailureListener(e -> {
                            Log.i("ashis", "putting image fail");

                            // Create a notification here
                        })
                        .addOnProgressListener(snapshot -> {
                            Log.i("ashis", "putting image on progress");

                            // Create a notification here
                            long progress = (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
//                            progressBar.setProgress((int) progress);
                        });
            }

            databaseReference.child(Objects.requireNonNull(user.getPhoneNumber()))
                    .child("name").setValue(data.getString("username","default"))
                    .addOnSuccessListener(unused -> {
                        // Create notification here
                    })
                    .addOnFailureListener(e -> {
                        // Create a notification here
                    });
        }
    }


    public static byte[] getByteArrayImage(Bitmap image) {
        int maxSize = Math.max(image.getHeight(), image.getWidth());
        return scalingImage(image, maxSize, 100);
    }

    @NonNull
    public static byte[] scalingImage(Bitmap realImage, int maxSize, int quality) {
        int newQuality = Math.min(quality, 100);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        makeSmall(realImage,maxSize).compress(Bitmap.CompressFormat.PNG, newQuality, outputStream);
        return outputStream.toByteArray();
    }

    /** Scaling the image */
    public static Bitmap makeSmall(Bitmap image, int maxSize) {
        int w = image.getWidth();
        int h = image.getHeight();
        float ratio = (float) w /(float) h;

        if(ratio > 1) {
            w = maxSize;
            h = (int) (w / ratio);
        } else {
            h = maxSize;
            w = (int) (h * ratio);
        }
        return Bitmap.createScaledBitmap(image,w,h,true);
    }

    public static boolean nameEdition(String userName, EditText name, ImageView nameEditIcon, boolean editOn, Context c) {
        if(!editOn) {
            nameEditIcon.setImageResource(R.drawable.ic_baseline_edit);
            name.setEnabled(false);
            name.clearFocus();
        } else {
            nameEditIcon.setImageResource(R.drawable.ic_baseline_check);
            name.setEnabled(true);
        }
        name.setText(userName);
        return editOn;
    }

    public static String extractPhoneNumber(String inputString) {
        StringBuilder extracted = new StringBuilder();

        for(char c : inputString.toCharArray())
            if(isNum(c)) extracted.append(c);

        if (extracted.length() > 10) extracted = extracted.delete(0,2);

        return extracted.toString();
    }

    private static boolean isNum(char c) {
        return (int)c>=48 && (int)c<=57;
    }

}
